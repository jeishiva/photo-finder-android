package com.experiment.facedetector.domain.usecase

import android.graphics.Bitmap
import com.experiment.facedetector.common.extension.toFaceBoundingBox
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.core.image.BitmapHelper
import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.FaceEmbeddingRequest
import com.experiment.facedetector.domain.processing.FaceEmbeddingExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Use case for extracting face embeddings with pluggable ML backends
 */
class ExtractEmbeddingsUseCase(
    private val embeddingExtractor: FaceEmbeddingExtractor,
    private val imageHelper: BitmapHelper,
) {

    /**
     * Extracts embeddings for multiple faces from a face embedding request.
     * @param request Contains the image and detected face regions
     * @return List of face embeddings with IDs and vectors
     */
    suspend operator fun invoke(request: FaceEmbeddingRequest): Result<List<FaceEmbedding>> {
        return try {
            withContext(Dispatchers.Default) {
                embeddingExtractor.initialize()
                val embeddings = extractEmbeddingsForFaces(request)
                Result.success(embeddings)
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to extract embeddings from request", e)
            Result.failure(e)
        }
    }

    /**
     * Extracts embedding for a single face bitmap.
     * @param faceBitmap Pre-cropped face image
     * @return Face embedding vector
     */
    suspend operator fun invoke(faceBitmap: Bitmap): Result<FloatArray> {
        return try {
            embeddingExtractor.initialize()
            val embedding = embeddingExtractor.extractEmbedding(faceBitmap)
            Result.success(embedding)
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to extract embedding from bitmap", e)
            Result.failure(e)
        }
    }

    private suspend fun extractEmbeddingsForFaces(
        faceEmbeddingRequest: FaceEmbeddingRequest,
    ): List<FaceEmbedding> {
        return try {
            faceEmbeddingRequest.faces.mapNotNull { face ->
                val croppedFace = imageHelper.cropFaceFromBitmap(
                    faceEmbeddingRequest.image,
                    face.toFaceBoundingBox()
                ) ?: run {
                    LogManager.w(TAG, "Failed to crop face from bitmap")
                    return@mapNotNull null
                }
                val embedding = embeddingExtractor.extractEmbedding(croppedFace)
                val faceId = generateFaceId()
                FaceEmbedding(
                    faceId = faceId,
                    embedding = embedding,
                )
            }
        } catch (e: Exception) {
            LogManager.w(TAG, "Failed to extract embeddings for faces")
            emptyList()
        }
    }

    private fun generateFaceId(): String = UUID.randomUUID().toString()

    /**
     * Cleanup resources when done
     */
    fun cleanup() {
        embeddingExtractor.cleanup()
        LogManager.d(TAG, "Use case resources cleaned up")
    }

    companion object {
        private const val TAG = "ExtractEmbeddingsUseCase"
    }
}