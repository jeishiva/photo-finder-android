package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.experiment.facedetector.data.local.entities.MediaWithFaces

@Dao
interface MediaWithFacesDao {
    @Transaction
    @Query(
        """
        SELECT * FROM media
        WHERE (modifiedAtMs < :cursorDate)
           OR (modifiedAtMs = :cursorDate AND mediaId < :cursorId)
        ORDER BY modifiedAtMs DESC, mediaId DESC
        LIMIT :limit
        """
    )
    suspend fun pageAfterAll(
        cursorDate: Long,
        cursorId: Long,
        limit: Int
    ): List<MediaWithFaces>

    @Transaction
    @Query(
        """
        SELECT m.*
        FROM media AS m
        WHERE EXISTS (
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
    suspend fun pageAfterFacesOnly(
        cursorDate: Long,
        cursorId: Long,
        limit: Int
    ): List<MediaWithFaces>
}
