package com.experiment.facedetector.data.local.index

import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.toMediaEntity
import com.experiment.facedetector.domain.index.MediaIndexer
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.domain.processing.ThumbnailGenerator
import com.experiment.facedetector.domain.repo.FaceRepository
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.repo.MediaRepository
import com.experiment.facedetector.domain.source.MediaSource
import com.experiment.facedetector.domain.source.SourceMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

class MediaIndexerImpl(
    private val source: MediaSource,
    private val mediaRepo: MediaRepository,
    private val faceRepo: FaceRepository,
    private val embeddings: FaceEmbeddingPipeline,
    private val thumbnails: ThumbnailGenerator,
    private val fingerPrint: MediaFingerPrint,
    private val pageSize: Int = 50,
    private val chunkSize: Int = 25,
    private val maxConcurrency: Int = 5
) : MediaIndexer {

    private val tag = "MediaIndexManager"

    override suspend fun refreshAll() = withContext(Dispatchers.IO) {
        LogManager.d(tag, "refreshAll start: pageSize=$pageSize, chunkSize=$chunkSize, concurrency=$maxConcurrency")
        var offset = 0
        var totalItems = 0
        var totalFaces = 0

        while (true) {
            val page = loadPage(offset, pageSize)
            if (page.isEmpty()) break
            val plan = upsertAndDetectChanges(page) // ids + changed subset
            val chunks = plan.changedItems.chunked(chunkSize)
            for ((index, chunk) in chunks.withIndex()) {
                LogManager.d(tag, "processing chunk $index size=${chunk.size} offset=$offset")
                val results = processChunkWithFlow(chunk, plan.idByStable).toList()
                val facesInChunk = results.sumOf { it.facesSaved }
                totalItems += results.size
                totalFaces += facesInChunk
                LogManager.d(tag, "chunk $index done: items=${results.size}, facesSaved=$facesInChunk")
            }
            offset += page.size
        }

        LogManager.d(tag, "refreshAll complete: totalItems=$totalItems, totalFaces=$totalFaces")
    }

    override suspend fun refreshPage(offset: Int, limit: Int) = withContext(Dispatchers.IO) {
        LogManager.d(tag, "refreshPage start: offset=$offset, limit=$limit")

        val page = loadPage(offset, limit)
        if (page.isEmpty()) {
            LogManager.d(tag, "refreshPage: empty page")
            return@withContext
        }

        val plan = upsertAndDetectChanges(page)

        val chunks = plan.changedItems.chunked(chunkSize)
        var totalFaces = 0
        for ((index, chunk) in chunks.withIndex()) {
            LogManager.d(tag, "processing chunk $index size=${chunk.size}")
            val results = processChunkWithFlow(chunk, plan.idByStable).toList()
            val facesInChunk = results.sumOf { it.facesSaved }
            totalFaces += facesInChunk
            LogManager.d(tag, "chunk $index done: items=${results.size}, facesSaved=$facesInChunk")
        }

        LogManager.d(tag, "refreshPage complete: facesSaved=$totalFaces")
    }

    private suspend fun loadPage(offset: Int, limit: Int): List<SourceMediaItem> {
        return try {
            source.list(offset = offset, limit = limit)
        } catch (e: Exception) {
            LogManager.e(tag, "loadPage failed: offset=$offset limit=$limit", e)
            emptyList()
        }
    }

    private suspend fun upsertAndDetectChanges(items: List<SourceMediaItem>): ChangePlan {
        if (items.isEmpty()) return ChangePlan(emptyMap(), emptyList())

        // Build NEW rows (mediaId=0 for auto-inc); compute new fingerprints in-memory
        val newRows: List<MediaEntity> = items.map {
            it.toMediaEntity(source.sourceType, fingerPrint /* auto-inc variant */)
        }

        // 1) Read OLD fingerprints by (source, sourceStableId) BEFORE upsert
        val stableIdsStr = items.map { it.stableId.toString() }
        val oldFpByStable: Map<String, String?> =
            mediaRepo.getFingerprintsBySource(source.sourceType.id, stableIdsStr) // NEW API (see below)

        // 2) Decide which items changed
        val changed = ArrayList<SourceMediaItem>(items.size)
        for (item in items) {
            val newFp = newRows.first { it.sourceStableId == item.stableId.toString() }.fingerprint
            val oldFp = oldFpByStable[item.stableId.toString()]
            val isChanged = (oldFp == null) || (oldFp != newFp)
            if (isChanged) changed.add(item)
        }

        // 3) Upsert ALL rows to keep metadata fresh (insert new / update existing)
        try {
            mediaRepo.upsertAll(newRows)
        } catch (e: Exception) {
            LogManager.e(tag, "upsertMediaRows failed for count=${newRows.size}", e)
        }

        // 4) Resolve DB-assigned mediaIds AFTER upsert
        val idsForPage: Map<String, Long> =
            mediaRepo.getIdsForSource(source.sourceType.id, stableIdsStr)

        // Convert to Long->Long for fast lookup
        val idByStable = HashMap<Long, Long>(idsForPage.size)
        for (item in items) {
            idsForPage[item.stableId.toString()]?.let { mediaId ->
                idByStable[item.stableId] = mediaId
            }
        }

        return ChangePlan(
            idByStable = idByStable,
            changedItems = changed
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun processChunkWithFlow(
        chunk: List<SourceMediaItem>,
        idByStable: Map<Long, Long>
    ): Flow<ItemProcessResult> {
        return chunk
            .asFlow()
            .flatMapMerge(concurrency = maxConcurrency) { item ->
                LogManager.d(tag, "processing item ${item.lastModifiedTime}")
                processSingleItemFlow(item, idByStable)
            }
    }

    private fun processSingleItemFlow(
        item: SourceMediaItem,
        idByStable: Map<Long, Long>
    ): Flow<ItemProcessResult> {
        return flow {
            val facesSaved = processOneItemAndPersist(item, idByStable)
            emit(ItemProcessResult(facesSaved = facesSaved))
        }
    }

    private suspend fun processOneItemAndPersist(
        item: SourceMediaItem,
        idByStable: Map<Long, Long>
    ): Int {
        val mediaId = idByStable[item.stableId]
        if (mediaId == null) {
            LogManager.w(tag, "mediaId not resolved for ${item.contentUri} (stableId=${item.stableId})")
            return 0
        }
        generateAndStoreThumbnail(item, mediaId)
        return extractAndStoreEmbeddings(item, mediaId)
    }

    private suspend fun generateAndStoreThumbnail(item: SourceMediaItem, mediaId: Long) {
        try {
            LogManager.d(tag, "generating thumbnail for ${item.contentUri}")
            val thumbPath = thumbnails.generateFromFile(
                filePath = item.contentUri.toString(),
                mediaId = mediaId
            )
            LogManager.d(tag, "generated path=$thumbPath")
            if (thumbPath != null) {
                mediaRepo.updateThumbnail(mediaId, thumbPath)
            } else {
                LogManager.d(tag, "thumbnail not generated for ${item.contentUri}")
            }
        } catch (e: Exception) {
            LogManager.e(tag, "thumbnail generation failed for ${item.contentUri}", e)
        }
    }

    private suspend fun extractAndStoreEmbeddings(
        item: SourceMediaItem,
        mediaId: Long
    ): Int {
        var facesSaved = 0
        try {
            val vectors = embeddings.extractEmbeddings(item)
            if (vectors.isEmpty()) {
                LogManager.d(tag, "No faces for mediaId=$mediaId")
                return facesSaved
            }
            val faces = ArrayList<FaceEntity>(vectors.size)
            for ((faceId, vector) in vectors) {
                faces.add(
                    FaceEntity(
                        faceId = faceId,
                        mediaOwnerId = mediaId,
                        embeddingData = vector
                    )
                )
            }
            if (faces.isNotEmpty()) {
                faceRepo.upsertAll(faces)
                facesSaved = faces.size
            }
        } catch (e: Exception) {
            LogManager.e(tag, "embedding extraction failed for ${item.contentUri}", e)
        }
        return facesSaved
    }

    private data class ItemProcessResult(val facesSaved: Int)

    private data class ChangePlan(
        val idByStable: Map<Long, Long>,
        val changedItems: List<SourceMediaItem>
    )
}
