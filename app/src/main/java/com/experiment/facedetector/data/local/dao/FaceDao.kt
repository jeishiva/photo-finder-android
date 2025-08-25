package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.experiment.facedetector.data.local.entities.FaceEntity

@Dao
interface FaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(face: FaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(faces: List<FaceEntity>)

    @Query("""
        SELECT * FROM face
        WHERE faceId = :faceId
        LIMIT 1
    """)
    suspend fun getById(faceId: String): FaceEntity?

    @Query("""
        SELECT * FROM face
        WHERE mediaOwnerId = :mediaId
        ORDER BY createdAt ASC
    """)
    suspend fun getByMediaId(mediaId: Long): List<FaceEntity>

    @Query("""
        SELECT faceId 
        FROM face
        WHERE mediaOwnerId = :mediaId
    """)
    suspend fun getFaceIdsForMedia(mediaId: Long): List<String>

    @Query("""
        DELETE FROM face
        WHERE faceId = :faceId
    """)
    suspend fun deleteById(faceId: String)

    @Query("""
        DELETE FROM face
        WHERE mediaOwnerId = :mediaId
    """)
    suspend fun deleteByMedia(mediaId: Long)

    @Query("SELECT COUNT(*) FROM face")
    suspend fun count(): Long
}

