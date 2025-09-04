package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.experiment.facedetector.data.local.entities.MediaWithFacesEntity
import com.experiment.facedetector.data.local.entities.ProcessedState

@Dao
interface MediaWithFacesDao {
    @Query(
        """
        SELECT m.*
        FROM media AS m
        WHERE m.processedState = :processed
          AND EXISTS (
              SELECT 1 FROM face AS f
              WHERE f.mediaOwnerId = m.mediaId
          )
          AND (
              m.modifiedAtMs < :cursorDate
              OR (m.modifiedAtMs = :cursorDate AND m.mediaId < :cursorId)
          )
        ORDER BY m.modifiedAtMs DESC, m.mediaId DESC
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
