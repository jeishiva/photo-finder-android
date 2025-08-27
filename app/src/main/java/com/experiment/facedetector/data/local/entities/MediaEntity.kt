package com.experiment.facedetector.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

import androidx.room.TypeConverters
import com.experiment.facedetector.data.local.converter.FloatArrayConverter

@Entity(
    tableName = "media",
    indices = [
        Index(value = ["dateModified", "mediaId"])
    ]
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val mediaId: Long = 0L,
    val source: String,                   // e.g. "CAMERA", "WHATSAPP, "CLOUD"
    val sourceStableId: String,           // stable ID from that source (e.g. MediaStore _ID, file path, cloud fileId)
    val contentUri: String,               // where to open the full image
    val thumbnailUri: String?,            // cached/generated thumbnail on disk
    val dateModified: Long? = null,       // last modified from source (epoch millis)
    val sizeBytes: Long? = null,          // file size
    val fingerprint: String? = null       // digest of size+modified+hash for reprocessing checks*/
)

@Entity(
    tableName = "face",
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["mediaId"],
            childColumns = ["mediaOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["mediaOwnerId"])
    ]
)
@TypeConverters(FloatArrayConverter::class)
data class FaceEntity(
    @PrimaryKey val faceId: String,
    val mediaOwnerId: Long,
    val embeddingData: FloatArray,      // stored as BLOB via converter
    val createdAt: Long = System.currentTimeMillis()
)

data class MediaWithFaces(
    @Embedded val media: MediaEntity,
    @Relation(
        parentColumn = "mediaId",
        entityColumn = "mediaOwnerId"
    )
    val faces: List<FaceEntity>
)