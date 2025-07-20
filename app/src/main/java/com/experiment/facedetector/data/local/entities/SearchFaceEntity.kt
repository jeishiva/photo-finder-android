package com.experiment.facedetector.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.experiment.facedetector.domain.entities.FaceBoundingBox
import com.experiment.facedetector.domain.entities.SearchFaceItem

@Entity(tableName = "search_face_items")
data class SearchFaceEntity(
    @PrimaryKey val faceId: String,
    val searchSessionId: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val thumbnailPath: String
) {
    fun toDomainModel() = SearchFaceItem(
        sessionId = searchSessionId,
        faceId = faceId,
        faceBoundingBox = FaceBoundingBox(left, top, right, bottom),
        thumbnailPath = thumbnailPath
    )

    companion object {
        fun fromDomain(item: SearchFaceItem) = SearchFaceEntity(
            faceId = item.faceId,
            searchSessionId = item.sessionId,
            left = item.faceBoundingBox.left,
            top = item.faceBoundingBox.top,
            right = item.faceBoundingBox.right,
            bottom = item.faceBoundingBox.bottom,
            thumbnailPath = item.thumbnailPath
        )
    }
}
