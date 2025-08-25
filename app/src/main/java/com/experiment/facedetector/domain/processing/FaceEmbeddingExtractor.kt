package com.experiment.facedetector.domain.processing

import android.graphics.Bitmap

/**
 * Abstract interface for face embedding extraction
 */
interface FaceEmbeddingExtractor {
    suspend fun initialize()
    suspend fun extractEmbedding(faceBitmap: Bitmap): FloatArray
    fun cleanup()

    companion object {
        const val INPUT_WIDTH = 112
        const val INPUT_HEIGHT = 112
        const val EMBEDDING_SIZE = 128
    }
}