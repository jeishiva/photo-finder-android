package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.entities.ProcessedState

@Dao
interface MediaWithFacesDao {

    /**
     * Load items *newer* than the given cursor (prepend).
     * Results are strictly newer (exclude cursor row itself).
     */
    @Transaction
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
              m.modifiedAtMs > :cursorDate
              OR (m.modifiedAtMs = :cursorDate AND m.mediaId > :cursorId)
          )
        ORDER BY m.modifiedAtMs DESC, m.mediaId DESC
        LIMIT :limit
        """
    )
    suspend fun pageNewerFacesOnly(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState = ProcessedState.PROCESSED,
    ): List<MediaWithFaces>

    /**
     * Load items *older* than the given cursor (append).
     * Results are strictly older (exclude cursor row itself).
     */
    @Transaction
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
    suspend fun pageOlderFacesOnly(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState = ProcessedState.PROCESSED,
    ): List<MediaWithFaces>

    @Transaction
    @Query(
        """
        SELECT *
        FROM media
        WHERE processedState = :processed
          AND (
              modifiedAtMs > :cursorDate
              OR (modifiedAtMs = :cursorDate AND mediaId > :cursorId)
          )
        ORDER BY modifiedAtMs DESC, mediaId DESC
        LIMIT :limit
        """
    )
    suspend fun pageNewerAll(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState = ProcessedState.PROCESSED,
    ): List<MediaWithFaces>

    @Transaction
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
    suspend fun pageOlderAll(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState = ProcessedState.PROCESSED,
    ): List<MediaWithFaces>



}
