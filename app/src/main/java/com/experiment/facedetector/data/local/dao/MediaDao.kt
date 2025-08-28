package com.experiment.facedetector.data.local.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.MediaIdFingerprintRow
import com.experiment.facedetector.data.local.entities.StableIdFingerprintRow
import com.experiment.facedetector.data.local.entities.StableIdToMediaIdRow

@Dao
interface MediaDao {
    @Upsert
    suspend fun upsert(media: MediaEntity)

    @Upsert
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
        SET thumbnailPath = :thumbnailUri
        WHERE mediaId = :mediaId
    """)
    suspend fun updateThumbnail(mediaId: Long, thumbnailUri: String?)

    @Query("SELECT mediaId, fingerprint FROM media WHERE mediaId IN (:ids)")
    suspend fun getFingerprints(ids: List<Long>): List<MediaIdFingerprintRow>


    @Query("""
        SELECT sourceStableId, mediaId
        FROM media
        WHERE sourceKey = :source
          AND sourceStableId IN (:stableIds)
    """)
    suspend fun getIdsForSourceRows(
        source: String,
        stableIds: List<String>
    ): List<StableIdToMediaIdRow>

    @Query("""
        SELECT sourceStableId, fingerprint
        FROM media
        WHERE sourceKey = :source AND sourceStableId IN (:stableIds)
    """)
    suspend fun getFingerprintsBySourceRows(
        source: String,
        stableIds: List<String>
    ): List<StableIdFingerprintRow>




}
