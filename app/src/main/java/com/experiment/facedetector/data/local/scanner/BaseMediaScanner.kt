package com.experiment.facedetector.data.local.scanner

import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.MediaErrorCode
import com.experiment.facedetector.data.local.entities.ProcessedState
import com.experiment.facedetector.data.local.entities.toMediaEntity
import com.experiment.facedetector.domain.entities.MediaSourceCursor
import com.experiment.facedetector.domain.entities.MediaSourcePage
import com.experiment.facedetector.domain.entities.SourceMediaItem
import com.experiment.facedetector.domain.entities.SyncConfig
import com.experiment.facedetector.domain.entities.SyncResult
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.domain.processing.ThumbnailGenerator
import com.experiment.facedetector.domain.repo.FaceRepository
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.repo.MediaRepository
import com.experiment.facedetector.domain.repo.MediaSourceCursorRepo
import com.experiment.facedetector.domain.repo.StableIdGenerator
import com.experiment.facedetector.domain.source.IdentifiablePagedMediaSource
import com.experiment.facedetector.domain.source.SyncableMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseMediaScanner(
    private val cursorRepo: MediaSourceCursorRepo,
    private val mediaRepo: MediaRepository,
    private val faceRepo: FaceRepository,
    private val embeddings: FaceEmbeddingPipeline,
    private val thumbnails: ThumbnailGenerator,
    private val stableIdGenerator: StableIdGenerator,
    private val fingerPrint: MediaFingerPrint,
    val source: IdentifiablePagedMediaSource,
) : SyncableMediaSource {

    private val tag = "MediaScanner"
    private val sourceType = source.sourceType

    override suspend fun sync(config: SyncConfig): SyncResult = withContext(Dispatchers.IO) {
        val sourceKey = sourceType.key
        LogManager.d(
            tag,
            "sync start: source=$sourceKey pageSize=${config.pageSize} " +
                    "maxItems=${config.maxItemsPerRun}" +
                    " chunkSize=${config.chunkSize} concurrency=${config.maxConcurrency}"
        )
        val startCursor: MediaSourceCursor? = loadCursor(sourceKey)
        var pagesScanned = 0
        var itemsFetched = 0
        var itemsUpserted = 0
        var itemsChanged = 0
        var itemsProcessed = 0
        var facesSavedTotal = 0
        var cursorAdvanced = false
        var budgetRemaining = config.maxItemsPerRun
        var currentCursor = startCursor

        try {
            pageLoop@ while (budgetRemaining > 0) {
                val pageResult = fetchPage(currentCursor, config.pageSize)

                when (pageResult) {
                    is PageResult.Success -> {
                        val page = pageResult.page
                        pagesScanned += 1
                        itemsFetched += page.items.size

                        LogManager.d(
                            tag,
                            "page $pagesScanned fetched=${page.items.size} afterCursor=$currentCursor hasMore=${page.hasMore}"
                        )

                        if (page.items.isEmpty()) {
                            page.nextCursor?.let { next ->
                                val advanced = advanceCursor(next)
                                if (advanced) {
                                    cursorAdvanced = true
                                }
                            }
                            break@pageLoop
                        }

                        val deltaPlan = upsertAndDetectChanges(page.items).also {
                            itemsUpserted += it.upsertedCount
                            itemsChanged += it.changedItems.size
                        }

                        if (deltaPlan.changedItems.isNotEmpty()) {
                            val chunks = deltaPlan.changedItems.chunked(config.chunkSize)
                            for ((index, chunk) in chunks.withIndex()) {
                                LogManager.d(
                                    tag,
                                    "processing chunk $index size=${chunk.size} page=$pagesScanned"
                                )

                                val results = processChunkWithFlow(
                                    chunk = chunk,
                                    idByStable = deltaPlan.idByStable,
                                    maxConcurrency = config.maxConcurrency
                                ).toList()

                                itemsProcessed += results.size

                                LogManager.d(
                                    tag,
                                    "chunk $index done: items=${results.size}"
                                )
                            }
                        } else {
                            LogManager.d(tag, "no changed items on page $pagesScanned (all skipped)")
                        }

                        page.nextCursor?.let { next ->
                            val advanced = advanceCursor(next)
                            if (advanced) {
                                cursorAdvanced = true
                            }
                        }
                        currentCursor = page.nextCursor
                        budgetRemaining -= page.items.size

                        if (!page.hasMore) {
                            LogManager.d(tag, "no more items from source; stopping")
                            break@pageLoop
                        }
                    }

                    is PageResult.Error -> {
                        LogManager.e(
                            tag,
                            "fetchPage failed: cursor=$currentCursor limit=${config.pageSize}",
                            pageResult.throwable
                        )
                        return@withContext SyncResult.Failure(
                            sourceKey = sourceKey,
                            reason = "Page fetch failed",
                            throwable = pageResult.throwable,
                            pagesScanned = pagesScanned,
                            itemsFetched = itemsFetched
                        )
                    }
                }
            }

            SyncResult.Success(
                sourceKey = sourceKey,
                pagesScanned = pagesScanned,
                itemsFetched = itemsFetched,
                itemsUpserted = itemsUpserted,
                itemsChanged = itemsChanged,
                itemsProcessed = itemsProcessed,
                facesSaved = facesSavedTotal,
                cursorAdvanced = cursorAdvanced
            )
        } catch (ce: CancellationException) {
            // propagate coroutine cancellation
            throw ce
        } catch (t: Throwable) {
            LogManager.e(tag, "sync failed: source=$sourceKey", t)
            SyncResult.Failure(
                sourceKey = sourceKey,
                reason = "Unhandled exception during sync",
                throwable = t,
                pagesScanned = pagesScanned,
                itemsFetched = itemsFetched
            )
        }
    }

    private suspend fun loadCursor(sourceKey: String): MediaSourceCursor? {
        val cursor = cursorRepo.get(sourceType)
        LogManager.d(tag, "loadCursor: source=$sourceKey cursor=$cursor")
        return cursor
    }

    private suspend fun fetchPage(
        after: MediaSourceCursor?,
        pageSize: Int,
    ): PageResult {
        return try {
            val page = source.listAfter(cursor = after, limit = pageSize)
            PageResult.Success(page)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LogManager.e(tag, "fetchPage failed: cursor=$after limit=$pageSize", e)
            PageResult.Error(e)
        }
    }

    private suspend fun upsertAndDetectChanges(sourceItems: List<SourceMediaItem>): MediaDelta {
        if (sourceItems.isEmpty()) {
            return MediaDelta(emptyMap(), emptyList(), 0)
        }

        val mediaEntities: List<MediaEntity> = sourceItems.map { sourceItem ->
            sourceItem.toMediaEntity(
                fingerPrint = fingerPrint,
                stableIdGenerator = stableIdGenerator
            )
        }

        val sourceStableIds = sourceItems.map { it.sourceStableId.toString() }
        val existingFingerprintsByStableId: Map<String, String?> =
            mediaRepo.getFingerprintsBySource(sourceType.key, sourceStableIds)

        val changedSourceItems = ArrayList<SourceMediaItem>(sourceItems.size)
        for (sourceItem in sourceItems) {
            val currentFingerprint = mediaEntities
                .first { it.sourceStableId == sourceItem.sourceStableId.toString() }
                .fingerprint
            val existingFingerprint = existingFingerprintsByStableId[sourceItem.sourceStableId.toString()]

            if (existingFingerprint == null || existingFingerprint != currentFingerprint) {
                changedSourceItems.add(sourceItem)
            }
        }

        val upsertedCount = try {
            mediaRepo.upsertAll(mediaEntities)
            mediaEntities.size
        } catch (e: Exception) {
            LogManager.e(TAG, "upsertAll failed for count=${mediaEntities.size}", e)
            0
        }

        val mediaIdsBySourceStableId: Map<String, Long> =
            mediaRepo.getIdsForSource(sourceType.key, sourceStableIds)
        val mediaIdMappings = HashMap<Long, Long>(mediaIdsBySourceStableId.size)

        for (sourceItem in sourceItems) {
            mediaIdsBySourceStableId[sourceItem.sourceStableId.toString()]?.let { mediaId ->
                mediaIdMappings[sourceItem.sourceStableId] = mediaId
            }
        }
        LogManager.d(
            TAG,
            "upsertAndDetectChanges: sourceItems=${sourceItems.size} " +
                    "upserted=$upsertedCount changed=${changedSourceItems.size}"
        )
        return MediaDelta(mediaIdMappings, changedSourceItems, upsertedCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun processChunkWithFlow(
        chunk: List<SourceMediaItem>,
        idByStable: Map<Long, Long>,
        maxConcurrency: Int,
    ): Flow<ItemProcessResult> {
        return chunk.asFlow().flatMapMerge(concurrency = maxConcurrency) { item ->
            LogManager.d(
                tag,
                "process item stableId=${item.sourceStableId} modified=${item.timeInfo.modifiedAtMs}"
            )
            processSingleItemFlow(item, idByStable)
        }
    }

    private fun processSingleItemFlow(
        item: SourceMediaItem,
        idByStable: Map<Long, Long>,
    ): Flow<ItemProcessResult> = flow {
        emit(item)
    }.map { sourceItem ->
        validateMediaId(sourceItem, idByStable)
    }.map { (sourceItem, mediaId) ->
        generateThumbnail(sourceItem, mediaId)
    }.map { (sourceItem, mediaId) ->
        extractEmbeddings(sourceItem, mediaId)
    }.catch { exception ->
        handleProcessingException(exception, item)
    }.map { itemProcessResult ->
        LogManager.d(
            tag,
            "processing item stableId=${item.sourceStableId} modified=${item.timeInfo.modifiedAtMs}"
        )
        updateProcessedState(itemProcessResult)
        itemProcessResult
    }

    private fun validateMediaId(
        sourceItem: SourceMediaItem,
        mediaIdByStableId: Map<Long, Long>,
    ): Pair<SourceMediaItem, Long> {
        val mediaId = mediaIdByStableId[sourceItem.sourceStableId] ?: run {
            LogManager.w(
                tag,
                "mediaId unresolved for ${sourceItem.contentPath} (stableId=${sourceItem.sourceStableId})"
            )
            throw MediaProcessingException(
                MediaErrorCode.STABLE_ID_UNRESOLVED,
                "Media ID unresolved for ${sourceItem.contentPath} (stableId=${sourceItem.sourceStableId})"
            )
        }
        return sourceItem to mediaId
    }

    private suspend fun generateThumbnail(
        sourceItem: SourceMediaItem,
        mediaId: Long,
    ): Pair<SourceMediaItem, Long> {
        LogManager.d(tag, "thumbnail: start mediaId=$mediaId uri=${sourceItem.contentPath}")
        val thumbnailResult = thumbnails.extractFromFile(sourceItem.contentPath.toString(), mediaId)
        thumbnailResult.fold(
            onSuccess = { thumbPath ->
                LogManager.d(tag, "thumbnail: done mediaId=$mediaId path=$thumbPath")
                mediaRepo.updateThumbnail(mediaId, thumbPath)
            },
            onFailure = { throwable ->
                LogManager.e(tag, "thumbnail: failed mediaId=$mediaId", throwable)
                throw MediaProcessingException(
                    MediaErrorCode.THUMBNAIL_FAILED,
                    "Thumbnail extraction failed: ${throwable.message}"
                )
            }
        )
        return sourceItem to mediaId
    }

    private suspend fun extractEmbeddings(
        sourceItem: SourceMediaItem,
        mediaId: Long,
    ): ItemProcessResult {
        LogManager.d(tag, "embedding: start mediaId=$mediaId")
        val extractEmbeddingResult = embeddings.extractEmbeddings(sourceItem)
        extractEmbeddingResult.fold(
            onSuccess = { embeddings ->
                val faces = embeddings.map { embedding ->
                    FaceEntity(
                        faceId = embedding.faceId,
                        mediaOwnerId = mediaId,
                        embeddingData = embedding.embedding,
                        createdAtMs = System.currentTimeMillis()
                    )
                }
                faceRepo.replaceFacesForMedia(mediaId, faces)
            },
            onFailure = { throwable ->
                LogManager.e(tag, "embedding: failed mediaId=$mediaId", throwable)
                throw MediaProcessingException(
                    MediaErrorCode.FACE_EXTRACTION_FAILED,
                    "Face extraction failed: ${throwable.message}"
                )
            }
        )
        return ItemProcessResult.Success(sourceItem)
    }

    private suspend fun FlowCollector<ItemProcessResult>.handleProcessingException(
        exception: Throwable,
        item: SourceMediaItem,
    ) {
        val result = when (exception) {
            is MediaProcessingException -> ItemProcessResult.Failed(
                item,
                exception.errorCode,
                exception.message ?: "Unknown error"
            )

            else -> ItemProcessResult.Failed(
                item,
                MediaErrorCode.OTHER,
                exception.message ?: "Unexpected error occurred"
            )
        }
        emit(result)
    }

    private suspend fun updateProcessedState(result: ItemProcessResult) {
        withContext(Dispatchers.IO) {
            when (result) {
                is ItemProcessResult.Success -> markSuccess(result.sourceMediaItem)
                is ItemProcessResult.Failed -> markFailed(
                    item = result.sourceMediaItem,
                    error = result.errorCode,
                    message = result.message
                )
            }
        }
    }

    suspend fun markFailed(
        item: SourceMediaItem,
        error: MediaErrorCode,
        message: String?,
    ) {
        LogManager.d(tag, "mark as processed for $item")
        mediaRepo.updateProcessedState(
            sourceKey = item.sourceKey,
            sourceStableId = item.sourceStableId,
            processedState = ProcessedState.FAILED,
            lastErrorCode = error,
            lastErrorMessage = message
        )
    }

    suspend fun markSuccess(item: SourceMediaItem) {
        LogManager.d(tag, "mark as processed for $item")
        mediaRepo.updateProcessedState(
            sourceKey = item.sourceKey,
            sourceStableId = item.sourceStableId,
            processedState = ProcessedState.PROCESSED,
            lastErrorCode = null,
            lastErrorMessage = null
        )
    }

    private suspend fun advanceCursor(next: MediaSourceCursor): Boolean {
        return try {
            cursorRepo.advanceIfNewer(sourceType, next, System.currentTimeMillis())
            LogManager.d(tag, "cursor advanced -> $next")
            true
        } catch (e: Exception) {
            LogManager.e(tag, "cursor advance failed: $next", e)
            false
        }
    }

    private data class MediaDelta(
        val idByStable: Map<Long, Long>,
        val changedItems: List<SourceMediaItem>,
        val upsertedCount: Int,
    )

    sealed class ItemProcessResult(open val sourceMediaItem: SourceMediaItem) {
        data class Success(
            override val sourceMediaItem: SourceMediaItem,
        ) : ItemProcessResult(sourceMediaItem)

        data class Failed(
            override val sourceMediaItem: SourceMediaItem,
            val errorCode: MediaErrorCode,
            val message: String,
        ) : ItemProcessResult(sourceMediaItem)
    }

    private class MediaProcessingException(
        val errorCode: MediaErrorCode,
        message: String,
    ) : Exception(message)

    companion object {
        private const val TAG = "BaseMediaScanner"
    }

    sealed class PageResult {
        data class Success(val page: MediaSourcePage<SourceMediaItem>) : PageResult()
        data class Error(val throwable: Throwable) : PageResult()
    }

}
