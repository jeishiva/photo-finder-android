package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.MediaWithFacesDao
import com.experiment.facedetector.data.local.entities.MediaWithFaces
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
    ): List<MediaWithFaces> {
        return mediaFacesWithDao.loadMediaBeforeCursor(cursorDate, cursorId, limit, processed)
    }
}
