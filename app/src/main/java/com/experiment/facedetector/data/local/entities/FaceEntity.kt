package com.experiment.facedetector.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.experiment.facedetector.data.local.converter.FloatArrayConverter

/**
 * One detected face tied to a media item.
 * Stores the embedding vector for similarity search and the bounding box
 * for UI/debug.
 */
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
        Index(value = ["mediaOwnerId"]),
        Index(value = ["embeddingHash"])
    ]
)
@TypeConverters(FloatArrayConverter::class)
data class FaceEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "faceRowId")
    val faceRowId: Long = 0L,

    @ColumnInfo(name = "mediaOwnerId")
    val mediaOwnerId: Long,

    /** Stable face identifier (e.g., UUID). */
    @ColumnInfo(name = "faceId")
    val faceId: String,

    /** Embedding vector (binary or Base64, depending on how you persist). */
    @ColumnInfo(name = "embeddingData")
    val embeddingData: FloatArray,

    /** Optional hash of embedding for quick lookup / deduplication. */
    @ColumnInfo(name = "embeddingHash")
    val embeddingHash: String? = null,

    /** Bounding box in image coordinates (for debugging / UI cropping). */
    @ColumnInfo(name = "boundingBox")
    val boundingBox: String? = null, // could be "x,y,w,h" JSON, or normalized floats

    /** When this face was detected/inserted (UTC ms). */
    @ColumnInfo(name = "createdAtMs")
    val createdAtMs: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceEntity

        if (faceRowId != other.faceRowId) return false
        if (mediaOwnerId != other.mediaOwnerId) return false
        if (createdAtMs != other.createdAtMs) return false
        if (this@FaceEntity.faceId != other.faceId) return false
        if (!embeddingData.contentEquals(other.embeddingData)) return false
        if (embeddingHash != other.embeddingHash) return false
        if (boundingBox != other.boundingBox) return false

        return true
    }

    override fun hashCode(): Int {
        var result = faceRowId.hashCode()
        result = 31 * result + mediaOwnerId.hashCode()
        result = 31 * result + createdAtMs.hashCode()
        result = 31 * result + faceId.hashCode()
        result = 31 * result + embeddingData.contentHashCode()
        result = 31 * result + (embeddingHash?.hashCode() ?: 0)
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        return result
    }
}
