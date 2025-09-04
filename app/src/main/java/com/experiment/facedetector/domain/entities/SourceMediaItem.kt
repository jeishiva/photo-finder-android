package com.experiment.facedetector.domain.entities

enum class MediaKind { IMAGE, VIDEO }

data class SourceMediaItem(
    val sourceKey: String,
    val sourceStableId: Long,
    val contentPath: String,
    val kind: MediaKind,
    val mediaInfo: MediaInfo,
    val timeInfo: MediaTimeInfo,
    val bucketInfo: BucketInfo,
    val image: ImageInfo? = null,
    val video: VideoInfo? = null
)

data class MediaInfo(
    val mimeType: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long
)

data class MediaTimeInfo(
    val createdAtMs: Long,
    val modifiedAtMs: Long,
    val generationModified: Long?
)

data class BucketInfo(
    val bucketId: Long?,
    val bucketName: String?
)

data class ImageInfo(
    val orientationDeg: Int
)

data class VideoInfo(
    val durationMs: Long,
    val rotationDeg: Int? = null
)