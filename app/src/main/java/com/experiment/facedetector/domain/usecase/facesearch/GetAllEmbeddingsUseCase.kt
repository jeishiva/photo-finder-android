package com.experiment.facedetector.domain.usecase.facesearch

import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.repo.FaceSearchRepository

class GetAllEmbeddingsUseCase(
    private val repository: FaceSearchRepository
) {
    suspend operator fun invoke(): List<FaceEmbedding> {
        return repository.getAllEmbeddings()
    }
}
