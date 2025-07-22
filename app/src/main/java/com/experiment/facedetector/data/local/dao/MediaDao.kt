package com.experiment.facedetector.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.experiment.facedetector.data.local.entities.FaceEmbeddingEntity
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.MediaWithFaces

@Dao
interface MediaDao {
    @Query("SELECT * FROM media ORDER BY mediaId DESC")
    fun getAllMedia(): PagingSource<Int, MediaEntity>

    @Query("SELECT * FROM media ORDER BY mediaId DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedMediaWithOffset(limit: Int, offset: Int): List<MediaEntity>

    @Query("SELECT * FROM media ORDER BY mediaId DESC")
    fun getPagedMedia(): PagingSource<Int, MediaEntity>

    @Query("SELECT * FROM media WHERE mediaId = :id")
    suspend fun getMediaEntityById(id: Long): MediaEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)

    @Transaction
    suspend fun insertMediaWithFaces(
        mediaEntity: List<MediaEntity>,
        faces: List<FaceEmbeddingEntity>
    ) {
        insertMediaList(mediaEntity)
        insertFaceEmbeddings(faces)
    }

    @Transaction
    @Query("SELECT * FROM media WHERE mediaId = :mediaId")
    suspend fun getMediaWithFaces(mediaId: Long): MediaWithFaces

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceEmbeddings(embeddings: List<FaceEmbeddingEntity>)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)

    @Query("SELECT mediaId FROM media WHERE mediaId IN (:mediaIds)")
    suspend fun getExistingMediaIds(mediaIds: List<Long>): List<Long>

    @Query("SELECT * FROM face_embedding WHERE mediaOwnerId IN (:mediaIds)")
    suspend fun getFacesForMediaIds(mediaIds: List<Long>): List<FaceEmbeddingEntity>

}
