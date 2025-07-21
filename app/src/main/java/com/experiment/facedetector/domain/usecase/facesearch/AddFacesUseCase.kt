package com.experiment.facedetector.domain.usecase.facesearch

import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.domain.repo.FaceSearchRepository

class AddFacesUseCase(
    private val repository: FaceSearchRepository
) {
    suspend operator fun invoke(faces: List<FaceSearchItem>) {
        repository.addFaces(faces)
    }
}
