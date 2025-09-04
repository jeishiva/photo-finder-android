package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.MediaWithFacesDao
import com.experiment.facedetector.data.local.entities.MediaWithFacesEntity
import com.experiment.facedetector.data.local.entities.ProcessedState
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository

class MediaWithFacesRepositoryImpl(
    private val mediaFacesWithDao: MediaWithFacesDao,
) : MediaWithFacesRepository {
    override suspend fun loadMediaBefore(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState,
    ): List<MediaWithFacesEntity> {
        return mediaFacesWithDao.loadMediaBeforeCursor(cursorDate, cursorId, limit, processed)
    }
}
