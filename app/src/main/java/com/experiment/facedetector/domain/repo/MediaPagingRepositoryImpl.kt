package com.experiment.facedetector.domain.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.experiment.facedetector.data.local.entities.toDomain
import com.experiment.facedetector.data.local.paging.GalleryKeySetPagingSource
import com.experiment.facedetector.data.local.paging.SearchPhotoPagingSource
import com.experiment.facedetector.data.local.repo.MediaPagingRepository
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaPagingRepositoryImpl(
    private val mediaRepository: MediaRepository,
    private val mediaWithFacesRepository: MediaWithFacesRepository,
) : MediaPagingRepository {

    override fun getGallerySourceFlow(): Flow<PagingData<MediaWithFacesDomain>> {
        return return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 20,
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

    override fun getSearchFaceFlow(): Flow<PagingData<MediaWithFacesDomain>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 20,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                SearchPhotoPagingSource(mediaWithFacesRepository)
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toDomain()
            }
        }
    }
}
