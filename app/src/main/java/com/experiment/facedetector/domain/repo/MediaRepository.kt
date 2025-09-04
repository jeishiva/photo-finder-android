package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.MediaErrorCode
import com.experiment.facedetector.data.local.entities.MediaWithFacesEntity
import com.experiment.facedetector.data.local.entities.ProcessedState
import com.experiment.facedetector.domain.entities.MediaIdFingerprint
import com.experiment.facedetector.domain.entities.SourceMediaItem

/**
 * Abstraction over media persistence.
 * Implement in the data layer using Room DAOs.
 */
interface MediaRepository {

    suspend fun upsertAll(items: List<MediaEntity>)

    suspend fun getMedia(mediaId: Long): MediaEntity?

    suspend fun updateThumbnail(mediaId: Long, thumbnailUri: String?)

    suspend fun getFingerprints(ids: List<Long>): List<MediaIdFingerprint>

    suspend fun getIdsForSource(
        source: String,
        stableIds: List<String>,
    ): Map<String, Long>

    suspend fun getFingerprintsBySource(
        source: String,
        stableIds: List<String>,
    ): Map<String, String?>

    suspend fun updateProcessedState(
        sourceStableId: Long,
        sourceKey: String,
        processedState: ProcessedState,
        lastErrorCode: MediaErrorCode?,
        lastErrorMessage: String?,
    )

    suspend fun updateProcessedState(
        mediaIds : List<Long>,
        processedState: ProcessedState,
        lastErrorCode: MediaErrorCode?,
        lastErrorMessage: String?,
    )

    suspend fun loadMediaBefore(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState ,
    ): List<MediaWithFacesEntity>


}
