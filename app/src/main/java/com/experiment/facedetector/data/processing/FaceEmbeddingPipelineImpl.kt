package com.experiment.facedetector.data.processing

import ExtractEmbeddingsUseCase
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.image.BitmapPool
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.face.FaceDetectionProcessor
import com.experiment.facedetector.domain.entities.FaceEmbeddingRequest
import com.experiment.facedetector.domain.source.SourceMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * File-based embedding pipeline that reuses your BitmapHelper decode path.
 * - Decodes upright with bounds (targetH/W) using BitmapHelper.decodeBitmap(uri, ...)
 * - Detects faces via FaceDetectionProcessor
 * - Extracts embeddings via ExtractEmbeddingsUseCase
 * - Returns List<Pair<String, FloatArray>> (UUID to embedding pairs)
 */
class FaceEmbeddingPipelineImpl(
    private val bitmapHelper: BitmapHelper,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val extractEmbeddingsUseCase: ExtractEmbeddingsUseCase,
    private val targetHeight: Int,
    private val targetWidth: Int
) : FaceEmbeddingPipeline {

    override suspend fun extractEmbeddings(mediaItem: SourceMediaItem): List<Pair<String, FloatArray>> {
        return withContext(Dispatchers.Default) {
            var decoded: android.graphics.Bitmap? = null
            try {
                decoded = bitmapHelper.decodeBitmap(
                    mediaItem.contentUri,
                    targetHeight,
                    targetWidth
                )
                val detected = faceDetectionProcessor.processImage(mediaItem)
                if (detected.faces.isEmpty()) {
                    LogManager.d(
                        TAG,
                        "No faces detected for file: ${mediaItem.contentUri}"
                    )
                    return@withContext emptyList<Pair<String, FloatArray>>()
                }
                val request = FaceEmbeddingRequest(
                    image = detected.image,
                    faces = detected.faces
                )
                val result = extractEmbeddingsUseCase(request)
                return@withContext result.fold(
                    onSuccess = { faceEmbeddings ->
                        if (faceEmbeddings.isEmpty()) {
                            emptyList<Pair<String, FloatArray>>()
                        } else {
                            faceEmbeddings.map { faceEmbedding ->
                                faceEmbedding.id to faceEmbedding.embedding
                            }
                        }
                    },
                    onFailure = { exception ->
                        LogManager.e(TAG, "Failed to extract embeddings", exception)
                        emptyList<Pair<String, FloatArray>>()
                    }
                )
            } catch (e: Exception) {
                LogManager.e(
                    TAG,
                    "Failed to extract embeddings for: ${mediaItem.contentUri}",
                    e
                )
                return@withContext emptyList<Pair<String, FloatArray>>()
            } finally {
                decoded?.let { bitmap ->
                    BitmapPool.put(bitmap)
                }
            }
        }
    }

    companion object {
        const val TAG = "DefaultFaceEmbeddingPipeline"
    }
}