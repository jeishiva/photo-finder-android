package com.experiment.facedetector.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val mediaId: Long,
    val contentUri: String,
    val thumbnailUri: String
)

@Entity(
    tableName = "face_embedding",
    foreignKeys = [ForeignKey(
        entity = MediaEntity::class,
        parentColumns = ["mediaId"],
        childColumns = ["mediaOwnerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["mediaOwnerId"])]
)

data class FaceEmbeddingEntity(
    @PrimaryKey val faceId: String,
    val mediaOwnerId: Long,
    val embeddingData: FloatArray
)

