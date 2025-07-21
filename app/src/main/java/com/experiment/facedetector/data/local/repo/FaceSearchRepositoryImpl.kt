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
                    for (face in mediaWithFaces.faces) {
                        println("faceEmbedding: $face")
                    }
                    val isSimilar = mediaWithFaces.faces.any { face ->
                        val embedding = face.embeddingData
                        searchEmbeddings.any { searchEmbedding ->
                            cosineSimilarity(
                                searchEmbedding,
                                embedding
                            ) >= AppConfig.PHOTO_SIMILARITY_THRESHOLD
                        }
                    }
                    LogManager.d(
                        TAG,
                        "${if (isSimilar) "Similar" else "Not similar"} faces found for mediaId: ${mediaWithFaces.media.mediaId}"
                    )
                    isSimilar
                }
            }
            .flowOn(Dispatchers.Default)
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

    companion object {
        private const val TAG = "FaceSearchRepository"
    }

}
