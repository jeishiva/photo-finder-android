package com.experiment.facedetector.data.local.repo

import androidx.paging.PagingData
import androidx.paging.filter
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.config.AppConfig
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.repo.FaceSearchRepository
import com.experiment.facedetector.domain.repo.MediaRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.math.sqrt

class FaceSearchRepositoryImpl(
    private val mediaRepo: MediaRepo
) : FaceSearchRepository {

    override suspend fun searchMatchingFacesPagedFlow(
        searchEmbeddings: List<FloatArray>,
    ): Flow<PagingData<MediaWithFaces>> {
        return mediaRepo.getPagedMediaWithFaces()
            .map { pagingData ->
                println("mediaWithFaces pagingData size: $pagingData")
                pagingData.filter { mediaWithFaces ->
                    val hasSimilarFace = hasMatchingFaceEmbedding(mediaWithFaces, searchEmbeddings)
                    logSimilarityResult(mediaWithFaces.media.mediaId, hasSimilarFace)
                    hasSimilarFace
                }
            }
            .flowOn(Dispatchers.Default)
    }

    private fun hasMatchingFaceEmbedding(
        mediaWithFaces: MediaWithFaces,
        searchEmbeddings: List<FloatArray>
    ): Boolean {
        return mediaWithFaces.faces.any { face ->
            val faceEmbedding = face.embeddingData
            searchEmbeddings.any { searchEmbedding ->
                val similarity = cosineSimilarity(searchEmbedding, faceEmbedding)
                similarity >= AppConfig.PHOTO_SIMILARITY_THRESHOLD
            }
        }
    }

    private fun logSimilarityResult(mediaId: Long, isSimilar: Boolean) {
        val resultText = if (isSimilar) "Similar" else "Not similar"
        LogManager.d(TAG, "$resultText faces found for mediaId: $mediaId")
    }

    fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        require(vec1.size == vec2.size) { "Vectors must be of the same size" }
        var dot = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in vec1.indices) {
            dot += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }
        return if (norm1 == 0f || norm2 == 0f) {
            0f
        } else {
            dot / (sqrt(norm1) * sqrt(norm2))
        }
    }

    companion object {
        private const val TAG = "FaceSearchRepository"
    }

}
