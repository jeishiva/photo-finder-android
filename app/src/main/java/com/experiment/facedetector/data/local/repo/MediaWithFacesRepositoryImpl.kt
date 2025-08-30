package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.MediaWithFacesDao

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.paging.BidirectionalKeysetPagingSource
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository

class MediaWithFacesRepositoryImpl(
    private val dao: MediaWithFacesDao
) : MediaWithFacesRepository {

    override fun pagerAll(pageSize: Int): Pager<Pair<Long, Long>, MediaWithFaces> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                BidirectionalKeysetPagingSource(
                    forwardLoader = { date, id, limit ->
                        dao.pageAfterFacesOnly(
                            cursorDate = date,
                            cursorId = id,
                            limit = pageSize
                        )
                    },
                    backwardLoader = { date, id, limit ->
                        dao.pageBeforeFacesOnly(
                            cursorDate = date,
                            cursorId = id,
                            limit = pageSize
                        )
                    }
                )
            }
        )
    }

    override fun pagerFacesOnly(pageSize: Int): Pager<Pair<Long, Long>, MediaWithFaces> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = 0,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                BidirectionalKeysetPagingSource(
                    forwardLoader = { date, id, limit ->
                        dao.pageAfterAll(
                            cursorDate = date,
                            cursorId = id,
                            limit = pageSize
                        )
                    },
                    backwardLoader = { date, id, limit ->
                        dao.pageBeforeAll(
                            cursorDate = date,
                            cursorId = id,
                            limit = pageSize
                        )
                    }
                )
            }
        )
    }
}
