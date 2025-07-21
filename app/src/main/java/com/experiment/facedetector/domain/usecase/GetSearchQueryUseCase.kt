package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.domain.repo.SearchQueryRepo

class GetSearchQueryUseCase(private val searchQueryRepo: SearchQueryRepo) {
    suspend operator fun invoke(sessionId: String): List<FaceSearchItem> = searchQueryRepo.getAll(sessionId)
}