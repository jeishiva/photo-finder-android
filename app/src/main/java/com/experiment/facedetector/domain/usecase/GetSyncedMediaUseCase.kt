package com.experiment.facedetector.domain.usecase

import androidx.paging.PagingData
import com.experiment.facedetector.data.local.repo.MediaPagingRepository
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import kotlinx.coroutines.flow.Flow

class GetSyncedMediaUseCase(
    private val mediaPagingRepository: MediaPagingRepository
) {
    operator fun invoke(): Flow<PagingData<MediaWithFacesDomain>> {
        return mediaPagingRepository.getGallerySourceFlow()
    }
}
