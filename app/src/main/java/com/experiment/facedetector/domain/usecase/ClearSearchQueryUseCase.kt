package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.domain.repo.SearchQueryRepo

class ClearSearchQueryUseCase(private val searchQueryRepo: SearchQueryRepo) {
    suspend operator fun invoke() = searchQueryRepo.clearAll()
}