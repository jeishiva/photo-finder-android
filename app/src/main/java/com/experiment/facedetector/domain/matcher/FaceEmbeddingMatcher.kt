package com.experiment.facedetector.domain.matcher

import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.config.AppConfig
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain

class FaceEmbeddingMatcher(
    private val embeddingMatcher: EmbeddingMatcher,
) {

    fun hasMatchingFaceEmbedding(
        mediaWithFaces: MediaWithFacesDomain,
        searchEmbeddings: List<FloatArray>,
        threshold: Float = AppConfig.PHOTO_SIMILARITY_THRESHOLD,
    ): Boolean {
        val hasMatch = mediaWithFaces.faces.any { face ->
            searchEmbeddings.any { searchEmbedding ->
                embeddingMatcher.isSimilar(searchEmbedding, face.embedding, threshold)
            }
        }
        logSimilarityResult(mediaWithFaces.media.mediaId, hasMatch)
        return hasMatch
    }

    private fun logSimilarityResult(mediaId: Long, isSimilar: Boolean) {
        val resultText = if (isSimilar) {
            "Similar"
        } else {
            "Not similar"
        }
        LogManager.d(TAG, "$resultText faces found for mediaId: $mediaId")
    }

    companion object {
        private const val TAG = "FaceEmbeddingMatcher"
    }
}