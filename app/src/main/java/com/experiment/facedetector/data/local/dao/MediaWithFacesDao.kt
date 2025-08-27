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
        WHERE (dateModified < :cursorDate)
           OR (dateModified = :cursorDate AND mediaId < :cursorId)
        ORDER BY dateModified DESC, mediaId DESC
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
            m.dateModified < :cursorDate
            OR (m.dateModified = :cursorDate AND m.mediaId < :cursorId)
        )
        ORDER BY m.dateModified DESC, m.mediaId DESC
        LIMIT :limit
        """
    )
    suspend fun pageAfterFacesOnly(
        cursorDate: Long,
        cursorId: Long,
        limit: Int
    ): List<MediaWithFaces>
}
