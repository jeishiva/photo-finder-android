package com.experiment.facedetector.domain.usecase

import androidx.paging.PagingData
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository
import kotlinx.coroutines.flow.Flow
import com.experiment.facedetector.data.local.entities.toDomain
import androidx.paging.map
import kotlinx.coroutines.flow.map

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.experiment.facedetector.data.local.paging.GalleryKeySetPagingSource
import com.experiment.facedetector.domain.repo.MediaRepository

class GetSyncedMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<PagingData<MediaWithFacesDomain>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 10,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                GalleryKeySetPagingSource(mediaRepository)
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toDomain()
            }
        }
    }
}
