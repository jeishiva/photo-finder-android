package com.experiment.facedetector.data.local.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.experiment.facedetector.data.local.dao.MediaWithFacesDao
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.filter.MediaFilter
import com.experiment.facedetector.domain.repo.MediaPagingRepository
import kotlinx.coroutines.flow.Flow

class MediaPagingRepositoryImpl(
    private val dao: MediaWithFacesDao,
    private val pageSize: Int = 40
) : MediaPagingRepository{
    override fun pagedMedia(filter: MediaFilter): Flow<PagingData<MediaWithFaces>> {
        val pagingSourceFactory = if (filter.facesOnly) {
            { dao.pagingFacesOnly() }
        } else {
            { dao.pagingAll() }
        }
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize / 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }
}
