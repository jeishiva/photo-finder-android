package com.experiment.facedetector.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class MediaWithFacesEntity(
    @Embedded val media: MediaEntity,
    @Relation(
        parentColumn = "mediaId",
        entityColumn = "mediaOwnerId"
    )
    val faces: List<FaceEntity>
)