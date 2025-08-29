package com.experiment.facedetector.domain.usecase

import androidx.paging.PagingData
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository
import kotlinx.coroutines.flow.Flow
import com.experiment.facedetector.data.local.entities.toDomain
import androidx.paging.map
import kotlinx.coroutines.flow.map

class GetSyncedMediaUseCase(
    private val mediaWithFacesRepository: MediaWithFacesRepository,
) {
    operator fun invoke(): Flow<PagingData<MediaWithFacesDomain>> {
        return mediaWithFacesRepository.pagerAll(pageSize = 20).flow.map { pagingData ->
            pagingData.map { mediaWithFaces ->
                mediaWithFaces.toDomain()
            }
        }
    }
}

