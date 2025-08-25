package com.experiment.facedetector.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.experiment.facedetector.data.local.entities.MediaEntity

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MediaEntity>)

    @Query("SELECT * FROM media WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getById(mediaId: Long): MediaEntity?

    @Query("""
        SELECT mediaId 
        FROM media 
        WHERE mediaId IN (:ids)
    """)
    suspend fun getExistingMediaIds(ids: List<Long>): List<Long>

    @Query("""
        UPDATE media
        SET thumbnailUri = :thumbnailUri
        WHERE mediaId = :mediaId
    """)
    suspend fun updateThumbnail(mediaId: Long, thumbnailUri: String?)

}
