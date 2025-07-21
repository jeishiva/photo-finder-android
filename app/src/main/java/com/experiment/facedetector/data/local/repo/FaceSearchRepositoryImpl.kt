package com.experiment.facedetector.data.local.repo

import android.graphics.Bitmap
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.domain.repo.FaceSearchRepository
import com.experiment.facedetector.domain.usecase.facesearch.ExtractEmbeddingsUseCase
import com.experiment.facedetector.image.BitmapHelper
import kotlin.math.sqrt

class FaceSearchRepositoryImpl(
    private val imageHelper: BitmapHelper,
    private val embeddingsUseCase: ExtractEmbeddingsUseCase
) : FaceSearchRepository {

    private val faceEmbeddings = mutableListOf<FaceEmbedding>()

    override suspend fun addFaces(faces: List<FaceSearchItem>) {
        faces.forEach { item ->
            val bitmap = imageHelper.loadBitmapFromPath(item.thumbnailPath)
            if (bitmap != null) {
                val embedding = embeddingsUseCase(bitmap)
                LogManager.d("FaceSearchRepository", "Embedding: $embedding")
                faceEmbeddings.add(FaceEmbedding(id = item.faceId, embedding = embedding))
            } else {
                LogManager.e(
                    "FaceSearchRepository",
                    "Failed to load bitmap from path: ${item.thumbnailPath}"
                )
            }
        }
    }

    override suspend fun getAllEmbeddings(): List<FaceEmbedding> = faceEmbeddings

    override suspend fun searchFace(
        targetEmbedding: FloatArray,
        threshold: Float
    ): List<FaceEmbedding> {
        return faceEmbeddings.filter { stored ->
            cosineSimilarity(targetEmbedding, stored.embedding) >= threshold
        }
    }

    fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dot = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in vec1.indices) {
            dot += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }
        return dot / (sqrt(norm1) * sqrt(norm2))
    }

}
