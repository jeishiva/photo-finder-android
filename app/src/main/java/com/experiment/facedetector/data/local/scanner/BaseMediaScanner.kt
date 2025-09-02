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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

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

    private val tag = "MediaIndexer"
    private val sourceType = source.sourceType

    override suspend fun sync(config: SyncConfig): SyncResult = withContext(Dispatchers.IO) {
        val sourceKey = sourceType.key
        LogManager.d(
            tag,
            "sync start: source=$sourceKey pageSize=${config.pageSize} maxItems=${config.maxItemsPerRun} chunkSize=${config.chunkSize} concurrency=${config.maxConcurrency}"
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
                val page = fetchPage(currentCursor, config.pageSize) ?: run {
                    return@withContext SyncResult.Failure(
                        sourceKey = sourceKey,
                        reason = "Page fetch failed",
                        throwable = null,
                        pagesScanned = pagesScanned,
                        itemsFetched = itemsFetched
                    )
                }

                pagesScanned++
                itemsFetched += page.items.size
                LogManager.d(
                    tag,
                    "page $pagesScanned fetched=${page.items.size} afterCursor=$currentCursor hasMore=${page.hasMore}"
                )

                if (page.items.isEmpty()) {
                    page.nextCursor?.let {
                        advanceCursor(it).also { cursorAdvanced = cursorAdvanced || it }
                    }
                    break@pageLoop
                }

                val plan = upsertAndDetectChanges(page.items).also {
                    itemsUpserted += it.upsertedCount
                    itemsChanged += it.changedItems.size
                }

                if (plan.changedItems.isNotEmpty()) {
                    val chunks = plan.changedItems.chunked(config.chunkSize)
                    for ((index, chunk) in chunks.withIndex()) {
                        LogManager.d(
                            tag,
                            "processing chunk $index size=${chunk.size} page=$pagesScanned"
                        )
                        val results = processChunkWithFlow(
                            chunk = chunk,
                            idByStable = plan.idByStable,
                            maxConcurrency = config.maxConcurrency
                        ).toList()

                        val facesInChunk = results.sumOf { it.facesSaved }
                        facesSavedTotal += facesInChunk
                        itemsProcessed += results.size
                        LogManager.d(
                            tag,
                            "chunk $index done: items=${results.size}, facesSaved=$facesInChunk (accumFaces=$facesSavedTotal)"
                        )
                    }
                } else {
                    LogManager.d(tag, "no changed items on page $pagesScanned (all skipped)")
                }

                page.nextCursor?.let {
                    advanceCursor(it).also { cursorAdvanced = cursorAdvanced || it }
                }
                currentCursor = page.nextCursor

                budgetRemaining -= page.items.size
                if (!page.hasMore) {
                    LogManager.d(tag, "no more items from source; stopping")
                    break@pageLoop
                }
            }
            LogManager.d(
                tag,
                "sync complete: pages=$pagesScanned fetched=$itemsFetched upserted=$itemsUpserted changed=$itemsChanged processed=$itemsProcessed faces=$facesSavedTotal cursorAdvanced=$cursorAdvanced"
            )
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
        val c = cursorRepo.get(sourceType)
        LogManager.d(tag, "loadCursor: source=$sourceKey cursor=$c")
        return c
    }

    private suspend fun fetchPage(
        after: MediaSourceCursor?,
        pageSize: Int,
    ): MediaSourcePage<SourceMediaItem>? {
        return try {
            source.listAfter(cursor = after, limit = pageSize)
        } catch (e: Exception) {
            LogManager.e(tag, "fetchPage failed: cursor=$after limit=$pageSize", e)
            null
        }
    }

    private suspend fun upsertAndDetectChanges(items: List<SourceMediaItem>): MediaDelta {
        if (items.isEmpty()) {
            return MediaDelta(emptyMap(), emptyList(), 0)
        }
        val newRows: List<MediaEntity> = items.map {
            it.toMediaEntity(
                fingerPrint = fingerPrint,
                stableIdGenerator = stableIdGenerator
            )
        }
        val stableIds = items.map { it.sourceStableId.toString() }
        val oldFpByStable: Map<String, String?> =
            mediaRepo.getFingerprintsBySource(sourceType.key, stableIds)

        val changed = ArrayList<SourceMediaItem>(items.size)
        for (item in items) {
            val newFp =
                newRows.first { it.sourceStableId == item.sourceStableId.toString() }.fingerprint
            val oldFp = oldFpByStable[item.sourceStableId.toString()]
            if (oldFp == null || oldFp != newFp) {
                changed.add(item)
            }
        }

        val upsertedCount = try {
            mediaRepo.upsertAll(newRows)
            newRows.size
        } catch (e: Exception) {
            LogManager.e(tag, "upsertAll failed for count=${newRows.size}", e)
            0
        }

        val idsForPage: Map<String, Long> = mediaRepo.getIdsForSource(sourceType.key, stableIds)

        val idByStable = HashMap<Long, Long>(idsForPage.size)
        for (item in items) {
            idsForPage[item.sourceStableId.toString()]?.let { mediaId ->
                idByStable[item.sourceStableId] = mediaId
            }
        }

        LogManager.d(
            tag,
            "upsertAndDetectChanges: items=${items.size} upserted=$upsertedCount changed=${changed.size}"
        )

        return MediaDelta(idByStable, changed, upsertedCount)
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
                "process item stableId=${item.sourceStableId} modified=${item.lastModifiedAtMs}"
            )
            processSingleItemFlow(item, idByStable)
        }
    }

    private fun processSingleItemFlow(
        item: SourceMediaItem,
        idByStable: Map<Long, Long>,
    ): Flow<ItemProcessResult> = flow {
        val facesSaved = processOneItem(item, idByStable)
        emit(ItemProcessResult(facesSaved))
    }

    private suspend fun processOneItem(item: SourceMediaItem, idByStable: Map<Long, Long>): Int {
        val mediaId = idByStable[item.sourceStableId] ?: run {
            LogManager.w(
                tag,
                "mediaId unresolved for ${item.contentPath} (stableId=${item.sourceStableId})"
            )
            return 0
        }

        // Mark PROCESSING
        mediaRepo.updateProcessedState(mediaId, ProcessedState.PROCESSING, null, null)

        // --- Thumbnails ---
        try {
            LogManager.d(tag, "thumbnail: start mediaId=$mediaId uri=${item.contentPath}")
            val thumbPath = thumbnails.extractFromFile(item.contentPath.toString(), mediaId)
            LogManager.d(tag, "thumbnail: done mediaId=$mediaId path=$thumbPath")
            if (thumbPath != null) {
                mediaRepo.updateThumbnail(mediaId, thumbPath)
            }
        } catch (e: Exception) {
            LogManager.e(tag, "thumbnail: failed mediaId=$mediaId uri=${item.contentPath}", e)
            mediaRepo.updateProcessedState(
                mediaId, ProcessedState.FAILED, MediaErrorCode.THUMBNAIL_FAILED, e.message
            )
            return 0
        }

        // --- Embeddings ---
        var facesSaved = 0
        try {
            LogManager.d(tag, "embedding: start mediaId=$mediaId")
            val vectors = embeddings.extractEmbeddings(item)
            if (vectors.isEmpty()) {
                LogManager.d(tag, "embedding: no faces mediaId=$mediaId")
                mediaRepo.updateProcessedState(mediaId, ProcessedState.PROCESSED, null, null)
                return 0
            }
            val faces = vectors.map { (faceId, vector) ->
                FaceEntity(
                    faceId = faceId,
                    mediaOwnerId = mediaId,
                    embeddingData = vector,
                    createdAtMs = System.currentTimeMillis()
                )
            }
            if (faces.isNotEmpty()) {
                faceRepo.upsertAll(faces)
                facesSaved = faces.size
                LogManager.d(tag, "embedding: faces saved=$facesSaved mediaId=$mediaId")
            }
            mediaRepo.updateProcessedState(mediaId, ProcessedState.PROCESSED, null, null)
        } catch (e: Exception) {
            LogManager.e(tag, "embedding: failed mediaId=$mediaId uri=${item.contentPath}", e)
            mediaRepo.updateProcessedState(
                mediaId, ProcessedState.FAILED, MediaErrorCode.FACE_EXTRACTION_FAILED, e.message
            )
        }
        return facesSaved
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

    private data class ItemProcessResult(val facesSaved: Int)
}
