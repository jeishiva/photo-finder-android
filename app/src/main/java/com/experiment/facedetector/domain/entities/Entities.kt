package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap

data class LocalImageItem(
    val uriString: String
)

data class FaceBoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class FaceDetectedItem(
    val faceId: String,
    val faceBoundingBox: FaceBoundingBox,
    val faceBitmap: Bitmap,
)

data class FaceSearchItem(
    val sessionId: String,
    val faceId: String,
    val faceBoundingBox: FaceBoundingBox,
    val thumbnailPath: String
)

data class FaceEmbedding(
    val id: String,
    val embedding: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceEmbedding

        if (id != other.id) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}