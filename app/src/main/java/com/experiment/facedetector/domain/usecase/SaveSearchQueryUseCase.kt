package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.repo.SearchQueryRepo

class SaveSearchQueryUseCase(private val searchQueryRepo: SearchQueryRepo) {
    suspend operator fun invoke(selectedFaceList: List<FaceDetectedItem>) : String {
        return searchQueryRepo.save(selectedFaceList)
    }
}