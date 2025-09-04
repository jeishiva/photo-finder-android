package com.experiment.facedetector.data.local.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.MediaErrorCode
import com.experiment.facedetector.data.local.entities.MediaIdFingerprintRow
import com.experiment.facedetector.data.local.entities.MediaWithFacesEntity
import com.experiment.facedetector.data.local.entities.ProcessedState
import com.experiment.facedetector.data.local.entities.StableIdFingerprintRow
import com.experiment.facedetector.data.local.entities.StableIdToMediaIdRow

@Dao
interface MediaDao {
    @Upsert
    suspend fun upsert(media: MediaEntity)

    @Upsert
    suspend fun upsertAll(items: List<MediaEntity>)

    @Query("SELECT * FROM media WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getMediaById(mediaId: Long): MediaEntity?

    @Query(
        """
            SELECT mediaId 
            FROM media 
            WHERE mediaId IN (:ids)
        """
    )
    suspend fun getExistingMediaIds(ids: List<Long>): List<Long>

    @Query("SELECT mediaId, fingerprint FROM media WHERE mediaId IN (:ids)")
    suspend fun getFingerprints(ids: List<Long>): List<MediaIdFingerprintRow>

    @Query(
        """
            SELECT sourceStableId, mediaId
            FROM media
            WHERE sourceKey = :source
              AND sourceStableId IN (:stableIds)
        """
    )
    suspend fun getIdsForSourceRows(
        source: String,
        stableIds: List<String>,
    ): List<StableIdToMediaIdRow>

    @Query(
        """
            SELECT sourceStableId, fingerprint
            FROM media
            WHERE sourceKey = :source AND sourceStableId IN (:stableIds)
        """
    )
    suspend fun getFingerprintsBySourceRows(
        source: String,
        stableIds: List<String>,
    ): List<StableIdFingerprintRow>


    // update thumbnail
    @Query(
        """
            UPDATE media
            SET thumbnailPath = :thumbnailUri
            WHERE mediaId = :mediaId
        """
    )
    suspend fun updateThumbnail(mediaId: Long, thumbnailUri: String?)

    @Query(
        """
    UPDATE media
    SET processedState = :processedState,
        lastErrorCode = :lastErrorCode,
        lastErrorMessage = :lastErrorMessage,
        lastProcessedAtMs = :lastProcessedAtMs,
        attemptCount = attemptCount + 1
    WHERE sourceStableId = :sourceStableId AND sourceKey = :sourceKey 
"""
    )
    suspend fun updateProcessedState(
        sourceStableId: Long,
        sourceKey: String,
        processedState: ProcessedState,
        lastErrorCode: MediaErrorCode?,
        lastErrorMessage: String?,
        lastProcessedAtMs: Long,
    )

    @Query(
        """
    UPDATE media
    SET processedState = :processedState,
        lastErrorCode = :lastErrorCode,
        lastErrorMessage = :lastErrorMessage,
        lastProcessedAtMs = :lastProcessedAtMs,
        attemptCount = attemptCount + 1
    WHERE mediaId IN (:mediaIds)
"""
    )
    suspend fun updateProcessedState(
        mediaIds: List<Long>,
        processedState: ProcessedState,
        lastErrorCode: MediaErrorCode?,
        lastErrorMessage: String?,
        lastProcessedAtMs: Long = System.currentTimeMillis()
    )

    @Query(
        """
        SELECT *
        FROM media
        WHERE processedState = :processed
          AND (
              modifiedAtMs < :cursorDate
              OR (modifiedAtMs = :cursorDate AND mediaId < :cursorId)
          )
        ORDER BY modifiedAtMs DESC, mediaId DESC
        LIMIT :limit
        """
    )
    suspend fun loadMediaBeforeCursor(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState = ProcessedState.PROCESSED,
    ): List<MediaWithFacesEntity>

}
