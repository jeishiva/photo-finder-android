package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.MediaWithFacesDao

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.paging.ForwardKeysetPagingSource
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository

class MediaWithFacesRepositoryImpl(
    private val dao: MediaWithFacesDao
) : MediaWithFacesRepository {

    override fun pagerAll(pageSize: Int): Pager<Pair<Long, Long>, MediaWithFaces> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ForwardKeysetPagingSource(
                    loader = { date, id, limit ->
                        dao.pageAfterAll(
                            cursorDate = date,
                            cursorId = id,
                            limit = limit
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
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ForwardKeysetPagingSource(
                    loader = { date, id, limit ->
                        dao.pageAfterFacesOnly(
                            cursorDate = date,
                            cursorId = id,
                            limit = limit
                        )
                    }
                )
            }
        )
    }
}
