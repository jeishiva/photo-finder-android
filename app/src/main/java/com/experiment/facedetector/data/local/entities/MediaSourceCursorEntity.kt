package com.experiment.facedetector.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.experiment.facedetector.domain.entities.MediaSourceCursor
import com.experiment.facedetector.domain.entities.MediaSourceType

/**
 * Persists the last processed cursor per source so workers can resume stateless.
 */
@Entity(tableName = "media_source_cursor")
data class MediaSourceCursorEntity(
    @PrimaryKey
    val sourceKey: MediaSourceType,

    /** Highest GENERATION_MODIFIED seen for this source (API 29+). */
    val lastGenerationModified: Long?,

    /** Highest DATE_MODIFIED (in SECONDS) seen among rows without generation. */
    val lastDateModifiedSec: Long?,

    /** Tie-breaker ID for rows that share the same token. */
    val lastId: Long?,

    /** For observability and backoff logic. UTC millis of last successful advance. */
    val updatedAtMs: Long
) {
    fun toDomainCursor(): MediaSourceCursor {
        return MediaSourceCursor(
            generationModified = lastGenerationModified,
            lastModifiedSeconds = lastDateModifiedSec,
            lastId = lastId
        )
    }

    companion object {
        fun fromDomain(
            sourceType: MediaSourceType,
            cursor: MediaSourceCursor,
            updatedAtMs: Long
        ): MediaSourceCursorEntity {
            return MediaSourceCursorEntity(
                sourceKey = sourceType,
                lastGenerationModified = cursor.generationModified,
                lastDateModifiedSec = cursor.lastModifiedSeconds,
                lastId = cursor.lastId,
                updatedAtMs = updatedAtMs
            )
        }
    }
}
