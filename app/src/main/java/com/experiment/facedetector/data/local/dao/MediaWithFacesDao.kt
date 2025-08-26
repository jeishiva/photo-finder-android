package com.experiment.facedetector.data.local.dao
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.experiment.facedetector.data.local.entities.MediaWithFaces

@Dao
interface MediaWithFacesDao {

    @Transaction
    @Query("""
        SELECT * FROM media
        ORDER BY dateModified DESC, mediaId DESC
    """)
    fun pagingAll(): PagingSource<Int, MediaWithFaces>

    @Transaction
    @Query("""
        SELECT m.* FROM media AS m
        WHERE EXISTS (
            SELECT 1 FROM face f
            WHERE f.mediaOwnerId = m.mediaId
            LIMIT 1
        )
        ORDER BY dateModified DESC, mediaId DESC
    """)
    fun pagingFacesOnly(): PagingSource<Int, MediaWithFaces>
}
