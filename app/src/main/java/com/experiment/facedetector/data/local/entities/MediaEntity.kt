package com.experiment.facedetector.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.experiment.facedetector.domain.entities.MediaKind

/**
 * Canonical local copy of a media item (image/video) used by UI and pipelines.
 *
 * Notes:
 * - All timestamps are UTC in milliseconds.
 * - `sourceKey` + `sourceStableId` uniquely identify the item within a source.
 * - `processedState` tracks thumbnail/embedding pipeline results per item.
 */
@Entity(
    tableName = "media",
    indices = [
        Index(value = ["sourceKey", "sourceStableId"], unique = true),
        Index(value = ["sourceKey"]),
        Index(value = ["processedState"]),
        Index(value = ["isDeleted"])
    ],
)
data class MediaEntity(
    @PrimaryKey
    @ColumnInfo(name = "mediaId")
    val mediaId: Long = 0L,

    // ---- Identity (per source) ----
    @ColumnInfo(name = "sourceKey")
    val sourceKey: String,

    @ColumnInfo(name = "sourceStableId")
    val sourceStableId: String,

    // Optional: keep for direct file access / debug
    @ColumnInfo(name = "contentUri")
    val contentUri: String,

    // ---- Descriptive ----
    @ColumnInfo(name = "mimeType")
    val mimeType: String,

    @ColumnInfo(name = "width")
    val width: Int,

    @ColumnInfo(name = "height")
    val height: Int,

    @ColumnInfo(name = "sizeBytes")
    val sizeBytes: Long,

    // Album grouping (if available)
    @ColumnInfo(name = "bucketId")
    val bucketId: Long?,

    @ColumnInfo(name = "bucketDisplayName")
    val bucketDisplayName: String?,

    //---- Image Specific ----
    // Images: display rotation (0/90/180/270). For videos this may be null.
    @ColumnInfo(name = "orientationDeg")
    val orientationDeg: Int?,

    @ColumnInfo(name = "mediaKind")
    val mediaKind: MediaKind,

    //---- video Specific ----
    @ColumnInfo(name = "durationMs")
    val durationMs: Long?,

    @ColumnInfo(name = "rotationDeg")
    val rotationDeg: Int?,

    // ---- Timeline ----
    /** For UI chronology (EXIF/DATE_TAKEN/DATE_ADDED fallback). */
    @ColumnInfo(name = "createdAtMs")
    val createdAtMs: Long,

    /** For sync/change detection (GENERATION_MODIFIED or DATE_MODIFIED). */
    @ColumnInfo(name = "modifiedAtMs")
    val modifiedAtMs: Long,

    /** Raw generation value when present (API 29+). */
    @ColumnInfo(name = "generationModified")
    val generationModified: Long?,

    // ---- Pipeline / Rendering ----
    /** Path to generated thumbnail on disk, if any. */
    @ColumnInfo(name = "thumbnailPath")
    val thumbnailPath: String?,

    /** Fingerprint used to detect changes (e.g., derived from size+modified+mime or a hash). */
    @ColumnInfo(name = "fingerprint")
    val fingerprint: String,

    /** Processing state for thumbnail + embeddings. */
    @ColumnInfo(name = "processedState")
    val processedState: ProcessedState = ProcessedState.PENDING,

    /** When we last completed or failed a stage. */
    @ColumnInfo(name = "lastProcessedAtMs")
    val lastProcessedAtMs: Long? = null,

    /** Number of processing attempts (sum of all stages). */
    @ColumnInfo(name = "attemptCount")
    val attemptCount: Int = 0,

    /** Short error code for last failure (e.g., FILE_NOT_FOUND, IO_TIMEOUT). */
    @ColumnInfo(name = "lastErrorCode")
    val lastErrorCode: MediaErrorCode? = null,

    /** Truncated error message from last failure (keep small, e.g., <= 512 chars). */
    @ColumnInfo(name = "lastErrorMessage")
    val lastErrorMessage: String? = null,

    // ---- Lifecycle / housekeeping ----
    /** Soft-deletion flag (presence sweep can flip this). UI should filter out deleted items. */
    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false,

    /** Row update time for observability. */
    @ColumnInfo(name = "updatedAtMs")
    val updatedAtMs: Long
)

/** High-level state of the processing pipeline for this media. */
enum class ProcessedState {
    PENDING,         // never attempted (new)
    PROCESSING,      // optional transient state if you want to set it
    PROCESSED,       // all required stages done
    FAILED
}

enum class MediaErrorCode(val code: Int) {
    THUMBNAIL_FAILED(100),
    FACE_EXTRACTION_FAILED(200),
    OTHER(10000),
}
