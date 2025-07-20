package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.domain.entities.SearchFaceItem
import com.experiment.facedetector.domain.repo.SearchQueryRepo

class GetSearchQueryUseCase(private val searchQueryRepo: SearchQueryRepo) {
    suspend operator fun invoke(sessionId: String): List<SearchFaceItem> = searchQueryRepo.getAll(sessionId)
}