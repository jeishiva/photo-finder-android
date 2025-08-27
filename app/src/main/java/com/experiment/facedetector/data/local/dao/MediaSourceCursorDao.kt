package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.experiment.facedetector.data.local.entities.MediaSourceCursorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaSourceCursorDao {

    @Upsert
    suspend fun upsert(entity: MediaSourceCursorEntity)

    @Query("SELECT * FROM media_source_cursor WHERE sourceKey = :sourceKey")
    suspend fun getOnce(sourceKey: String): MediaSourceCursorEntity?

    fun observe(sourceKey: String): Flow<MediaSourceCursorEntity?>

    @Query(
        """
        UPDATE media_source_cursor
        SET lastGenerationModified = :lastGenerationModified,
            lastDateModifiedSec   = :lastDateModifiedSec,
            lastId                = :lastId,
            updatedAtMs           = :updatedAtMs
        WHERE sourceKey = :sourceKey
    """
    )
    suspend fun updateCursor(
        sourceKey: String,
        lastGenerationModified: Long?,
        lastDateModifiedSec: Long?,
        lastId: Long?,
        updatedAtMs: Long
    )

    @Query("DELETE FROM media_source_cursor WHERE sourceKey = :sourceKey")
    suspend fun clear(sourceKey: String)
}
