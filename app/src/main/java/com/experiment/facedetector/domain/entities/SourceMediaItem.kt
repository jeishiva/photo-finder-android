package com.experiment.facedetector.domain.entities

enum class MediaKind { IMAGE, VIDEO }

data class SourceMediaItem(
    val sourceKey: String,
    val sourceStableId: Long,
    val contentPath: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val createdAtMs: Long,
    val lastModifiedAtMs: Long,
    val generationModified: Long?,
    val bucketId: Long?,
    val bucketDisplayName: String?,
    val kind: MediaKind,
    val image: ImageInfo? = null,
    val video: VideoInfo? = null
)

data class ImageInfo(
    val orientationDeg: Int
)

data class VideoInfo(
    val durationMs: Long,
    val rotationDeg: Int? = null
)
