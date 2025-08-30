package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.MediaWithFacesDao

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.paging.BidirectionalKeySetPagingSource
import com.experiment.facedetector.data.local.paging.GalleryKeySetPagingSource
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository

class MediaWithFacesRepositoryImpl(
    private val mediaFacesWithDao: MediaWithFacesDao,
) : MediaWithFacesRepository {

    override fun pagerAll(pageSize: Int): Pager<Pair<Long, Long>, MediaWithFaces> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                GalleryKeySetPagingSource(
                    mediaFacesWithDao
                )
            }
        )
    }

    override fun pagerFacesOnly(pageSize: Int): Pager<Pair<Long, Long>, MediaWithFaces> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                BidirectionalKeySetPagingSource(
                    forwardLoader = { date, id, limit ->
                        val mediaWithFaces = mediaFacesWithDao.pageNewerFacesOnly(
                            cursorDate = date,
                            cursorId = id,
                            limit = pageSize
                        )
                        val ids = mediaWithFaces.map { it.media.mediaId }
                        LogManager.d("xpaging", "forwardLoader: ids=$ids")
                        mediaWithFaces
                    },
                    backwardLoader = { date, id, limit ->
                        val mediaWithFaces = mediaFacesWithDao.pageOlderFacesOnly(
                            cursorDate = date,
                            cursorId = id,
                            limit = pageSize
                        )
                        val ids = mediaWithFaces.map { it.media.mediaId }
                        LogManager.d("xpaging", "forwardLoader: ids=$ids")
                        mediaWithFaces
                    }
                )
            }
        )
    }
}
