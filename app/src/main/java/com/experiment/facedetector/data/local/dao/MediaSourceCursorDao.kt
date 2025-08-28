package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.experiment.facedetector.data.local.entities.SourceMediaCursor
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaSourceCursorDao {

    @Upsert
    suspend fun upsert(entity: SourceMediaCursor)

    @Query("SELECT * FROM media_source_cursor WHERE sourceKey = :sourceKey")
    suspend fun getOnce(sourceKey: String): SourceMediaCursor?

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
