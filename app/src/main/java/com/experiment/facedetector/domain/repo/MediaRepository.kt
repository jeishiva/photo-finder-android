package com.experiment.facedetector.domain.repo

import androidx.paging.Pager
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.MediaErrorCode
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.entities.ProcessedState
import com.experiment.facedetector.domain.entities.MediaIdFingerprint

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
        mediaId: Long,
        processedState: ProcessedState,
        lastErrorCode: MediaErrorCode?,
        lastErrorMessage: String?,
    )

    suspend fun loadMediaBefore(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState ,
    ): List<MediaWithFaces>


}
