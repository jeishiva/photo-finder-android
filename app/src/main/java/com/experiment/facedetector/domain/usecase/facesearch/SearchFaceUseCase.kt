package com.experiment.facedetector.domain.usecase.facesearch

import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.repo.FaceSearchRepository

class SearchFaceUseCase(
    private val repository: FaceSearchRepository
) {
    suspend operator fun invoke(
        targetEmbedding: FloatArray,
        threshold: Float = 0.7f
    ): List<FaceEmbedding> {
        return repository.searchFace(targetEmbedding, threshold)
    }
}
