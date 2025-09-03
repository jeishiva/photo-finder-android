package com.experiment.facedetector.data.processing

import com.experiment.facedetector.domain.usecase.ExtractEmbeddingsUseCase
import com.experiment.facedetector.core.image.BitmapHelper
import com.experiment.facedetector.core.image.BitmapPool
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.core.face.FaceDetectionProcessor
import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.FaceEmbeddingRequest
import com.experiment.facedetector.domain.entities.SourceMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * File-based embedding pipeline that reuses your BitmapHelper decode path.
 * - Decodes upright with bounds (targetH/W) using BitmapHelper.decodeBitmap(uri, ...)
 * - Detects faces via FaceDetectionProcessor
 * - Extracts embeddings via com.experiment.facedetector.domain.usecase.ExtractEmbeddingsUseCase
 */
class FaceEmbeddingPipelineImpl(
    private val bitmapHelper: BitmapHelper,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val extractEmbeddingsUseCase: ExtractEmbeddingsUseCase,
    private val targetHeight: Int,
    private val targetWidth: Int,
) : FaceEmbeddingPipeline {

    override suspend fun extractEmbeddings(mediaItem: SourceMediaItem): Result<List<FaceEmbedding>> {
        return withContext(Dispatchers.Default) {
            var decoded: android.graphics.Bitmap? = null
            try {
                decoded = bitmapHelper.decodeBitmap(
                    mediaItem.contentPath,
                    targetHeight,
                    targetWidth
                )
                val detected = faceDetectionProcessor.processImage(mediaItem)
                if (detected.faces.isEmpty()) {
                    LogManager.d(
                        TAG,
                        "No faces detected for file: ${mediaItem.contentPath}"
                    )
                    return@withContext Result.success(emptyList<FaceEmbedding>())
                }
                val result = extractEmbeddingsUseCase(FaceEmbeddingRequest(
                    image = detected.image,
                    faces = detected.faces
                ))
                return@withContext result
            } catch (exception: Exception) {
                LogManager.e(
                    TAG,
                    "Failed to extract embeddings for: ${mediaItem.contentPath}",
                    exception
                )
                return@withContext Result.failure(exception)
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