package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.data.local.entities.MediaEntity

/**
 * Abstraction over media persistence.
 * Implement in the data layer using Room DAOs.
 */
interface MediaRepository {

    /**
     * Insert or update a batch of media rows.
     */
    suspend fun upsertAll(items: List<MediaEntity>)

    /**
     * Update the thumbnail path/uri for a media row.
     */
    suspend fun updateThumbnail(mediaId: Long, thumbnailUri: String?)

    /**
     * Optional convenience for filtering already-known media.
     * Returns the subset of ids that already exist.
     */
    suspend fun getExistingMediaIds(ids: List<Long>): List<Long>
}
