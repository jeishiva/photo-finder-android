package com.experiment.facedetector.domain.usecase.facesearch

import androidx.paging.PagingData
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.filter.MediaFilter
import com.experiment.facedetector.domain.repo.MediaPagingRepository
import kotlinx.coroutines.flow.Flow


class SearchPhotosPagedUseCase(val mediaPagingRepository: MediaPagingRepository) {
    suspend operator fun invoke(filter: MediaFilter) : Flow<PagingData<MediaWithFaces>>{
        return mediaPagingRepository.pagedMedia(filter)
    }
}
