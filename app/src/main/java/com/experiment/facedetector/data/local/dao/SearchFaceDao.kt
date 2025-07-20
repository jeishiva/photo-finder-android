package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.experiment.facedetector.data.local.entities.SearchFaceEntity

@Dao
interface SearchFaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(faces: List<SearchFaceEntity>)

    @Query("DELETE FROM search_face_items")
    suspend fun clearAll()

    @Query("SELECT * FROM search_face_items WHERE searchSessionId = :sessionId")
    suspend fun getAllBySession(sessionId: String): List<SearchFaceEntity>
}
