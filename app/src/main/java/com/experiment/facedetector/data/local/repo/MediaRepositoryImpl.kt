package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.repo.MediaRepository

class MediaRepositoryImpl(
    private val mediaDao: MediaDao
) : MediaRepository {

    override suspend fun upsertAll(items: List<MediaEntity>) {
        if (items.isEmpty()) {
            return
        } else {
            mediaDao.upsertAll(items)
        }
    }

    override suspend fun updateThumbnail(mediaId: Long, thumbnailUri: String?) {
        mediaDao.updateThumbnail(mediaId, thumbnailUri)
    }

    override suspend fun getExistingMediaIds(ids: List<Long>): List<Long> {
        return mediaDao.getExistingMediaIds(ids)
    }
}

