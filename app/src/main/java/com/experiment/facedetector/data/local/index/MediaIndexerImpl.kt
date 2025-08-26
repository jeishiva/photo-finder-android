package com.experiment.facedetector.data.local.index

import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.index.MediaIndexer
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.domain.processing.ThumbnailGenerator
import com.experiment.facedetector.domain.repo.FaceRepository
import com.experiment.facedetector.domain.repo.MediaRepository
import com.experiment.facedetector.domain.source.MediaSource
import com.experiment.facedetector.domain.source.MediaSourceWithLookup
import com.experiment.facedetector.domain.source.SourceMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

/**
 * MediaIndexManager implementation that:
 *  - pages the MediaSource (offset/limit)
 *  - upserts media
 *  - generates thumbnails
 *  - extracts embeddings and upserts faces
 *  - processes each page in chunks, with flow-based concurrency
 */
class MediaIndexerImpl(
    private val source: MediaSource,
    private val mediaRepo: MediaRepository,
    private val faceRepo: FaceRepository,
    private val embeddings: FaceEmbeddingPipeline,
    private val thumbnails: ThumbnailGenerator,
    private val pageSize: Int = 100,
    private val chunkSize: Int = 5,
    private val maxConcurrency: Int = 3
) : MediaIndexer {

    private val tag = "MediaIndexManager"

    override suspend fun refreshAll() = withContext(Dispatchers.IO) {
        LogManager.d(
            tag,
            "refreshAll start: pageSize=$pageSize, chunkSize=$chunkSize, concurrency=$maxConcurrency"
        )
        var offset = 0
        var totalItems = 0
        var totalFaces = 0
        while (true) {
            val page = loadPage(offset, pageSize)
            if (page.isEmpty()) {
                break
            }
            upsertMediaRows(page)
            val chunks = page.chunked(chunkSize)
            for ((index, chunk) in chunks.withIndex()) {
                LogManager.d(tag, "processing chunk $index size=${chunk.size} offset=$offset")
                val results = processChunkWithFlow(chunk).toList()
                val facesInChunk = results.sumOf { it.facesSaved }
                totalItems += results.size
                totalFaces += facesInChunk
                LogManager.d(
                    tag,
                    "chunk $index done: items=${results.size}, facesSaved=$facesInChunk"
                )
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
        upsertMediaRows(page)
        val chunks = page.chunked(chunkSize)
        var totalFaces = 0
        for ((index, chunk) in chunks.withIndex()) {
            LogManager.d(tag, "processing chunk $index size=${chunk.size}")
            val results = processChunkWithFlow(chunk).toList()
            val facesInChunk = results.sumOf { it.facesSaved }
            totalFaces += facesInChunk
            LogManager.d(tag, "chunk $index done: items=${results.size}, facesSaved=$facesInChunk")
        }
        LogManager.d(tag, "refreshPage complete: facesSaved=$totalFaces")
    }

    override suspend fun processSingleItem(mediaId: Long): Boolean = withContext(Dispatchers.IO) {
        // For processSingle we need to fetch a SourceMediaItem by its stable id.
        // If your MediaSource supports lookup, use it; otherwise return false.
        val item = getByStableIdIfSupported(mediaId)
        if (item == null) {
            LogManager.d(
                tag,
                "processSingle: source does not support lookup or item not found (mediaId=$mediaId)"
            )
            return@withContext false
        }
        try {
            val row = MediaEntity(
                mediaId = item.stableId,
                contentUri = item.contentUri.toString(),
                thumbnailUri = null
            )
            mediaRepo.upsertAll(listOf(row))
        } catch (e: Exception) {
            LogManager.e(tag, "processSingle: failed to upsert media row", e)
            return@withContext false
        }

        val facesSaved = processOneItemAndPersist(item)
        return@withContext facesSaved > 0
    }

    private suspend fun loadPage(offset: Int, limit: Int): List<SourceMediaItem> {
        return try {
            source.list(offset = offset, limit = limit)
        } catch (e: Exception) {
            LogManager.e(tag, "loadPage failed: offset=$offset limit=$limit", e)
            emptyList()
        }
    }

    private suspend fun upsertMediaRows(items: List<SourceMediaItem>) {
        if (items.isEmpty()) {
            return
        }
        val rows = ArrayList<MediaEntity>(items.size)
        for (item in items) {
            val row = MediaEntity(
                mediaId = item.stableId,
                contentUri = item.contentUri.toString(),
                thumbnailUri = null
            )
            rows.add(row)
        }
        try {
            mediaRepo.upsertAll(rows)
        } catch (e: Exception) {
            LogManager.e(tag, "upsertMediaRows failed for count=${rows.size}", e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun processChunkWithFlow(chunk: List<SourceMediaItem>): Flow<ItemProcessResult> {
        return chunk
            .asFlow()
            .flatMapMerge(concurrency = maxConcurrency) { item ->
                processSingleItemFlow(item)
            }
    }

    private fun processSingleItemFlow(item: SourceMediaItem): Flow<ItemProcessResult> {
        return flow {
            val facesSaved = processOneItemAndPersist(item)
            emit(ItemProcessResult(facesSaved = facesSaved))
        }
    }

    private suspend fun processOneItemAndPersist(item: SourceMediaItem): Int {
        generateAndStoreThumbnail(item)
        return extractAndStoreEmbeddings(item)
    }

    private suspend fun generateAndStoreThumbnail(item: SourceMediaItem) {
        try {
            LogManager.d(tag, "generating thumbnail for ${item.contentUri}")
            val thumbPath = thumbnails.generateFromFile(
                filePath = item.contentUri.toString(), mediaId = item.stableId
            )
            LogManager.d(tag, "generated path=$thumbPath")
            if (thumbPath != null) {
                mediaRepo.updateThumbnail(item.stableId, thumbPath)
            } else {
                LogManager.d(tag, "thumbnail not generated for ${item.contentUri}")
            }
        } catch (e: Exception) {
            LogManager.e(tag, "thumbnail generation failed for ${item.contentUri}", e)
        }
    }

    private suspend fun extractAndStoreEmbeddings(item: SourceMediaItem): Int {
        var facesSaved = 0
        try {
            val vectors = embeddings.extractEmbeddings(item)
            if (vectors.isEmpty()) {
                return facesSaved
            }
            val faces = ArrayList<FaceEntity>(vectors.size)
            for ((faceId, vector) in vectors) {
                val face = FaceEntity(
                    faceId = faceId,
                    mediaOwnerId = item.stableId,
                    embeddingData = vector
                )
                faces.add(face)
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

    private suspend fun getByStableIdIfSupported(mediaId: Long): SourceMediaItem? {
        if (source is MediaSourceWithLookup) {
            try {
                return source.getByStableId(mediaId)
            } catch (e: Exception) {
                LogManager.e(tag, "getByStableId failed for mediaId=$mediaId", e)
                return null
            }
        } else {
            return null
        }
    }

    private data class ItemProcessResult(
        val facesSaved: Int
    )
}

