package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.MediaIdFingerprint
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

    override suspend fun getFingerprints(ids: List<Long>): List<MediaIdFingerprint> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val rows = mediaDao.getFingerprints(ids)
        return rows.map {
            MediaIdFingerprint(
                mediaId = it.mediaId,
                fingerprint = it.fingerprint
            )
        }
    }

    override suspend fun getIdsForSource(
        source: String,
        stableIds: List<String>
    ): Map<String, Long> {
        if (stableIds.isEmpty()) {
            return emptyMap()
        }
        val rows = mediaDao.getIdsForSourceRows(source, stableIds)
        val out = HashMap<String, Long>(rows.size)
        for (r in rows) {
            out[r.sourceStableId] = r.mediaId
        }
        return out
    }

    override suspend fun getFingerprintsBySource(
        source: String,
        stableIds: List<String>
    ): Map<String, String?> {
        val rows = mediaDao.getFingerprintsBySourceRows(source, stableIds)
        return HashMap<String, String?>(rows.size).apply {
            for (r in rows) this[r.sourceStableId] = r.fingerprint
        }
    }

}

