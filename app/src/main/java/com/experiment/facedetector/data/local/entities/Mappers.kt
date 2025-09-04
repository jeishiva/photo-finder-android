package com.experiment.facedetector.data.local.entities

import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.MediaDomain
import com.experiment.facedetector.domain.entities.MediaKind
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.entities.SourceMediaItem
import com.experiment.facedetector.domain.repo.StableIdGenerator

// Maps the unified SourceMediaItem (image or video) into MediaEntity.
// - Uses common fields for both types
// - Fills image-only and video-only fields when present (nullable in Room)

fun SourceMediaItem.toMediaEntity(
    fingerPrint: MediaFingerPrint,
    stableIdGenerator: StableIdGenerator,
): MediaEntity {

    val fingerPrint = fingerPrint.generate(
        sourceStableId = this.sourceStableId.toString(),
        lastModified = this.timeInfo.modifiedAtMs,
        sizeBytes = this.mediaInfo.sizeBytes
    )

    val kind: MediaKind = if (this.video != null) {
        MediaKind.VIDEO
    } else {
        MediaKind.IMAGE
    }

    return MediaEntity(
        mediaId = stableIdGenerator.generate(buildString {
            append(this@toMediaEntity.sourceKey)
            append("_")
            append(this@toMediaEntity.sourceStableId.toString())
        }),
        // identity
        sourceKey = sourceKey,
        sourceStableId = this.sourceStableId.toString(),
        contentPath = this.contentPath,
        mediaKind = kind, // new: IMAGE or VIDEO

        // descriptive
        mimeType = this.mediaInfo.mimeType,
        width = this.mediaInfo.width,
        height = this.mediaInfo.height,
        sizeBytes = this.mediaInfo.sizeBytes,
        bucketId = this.bucketInfo.bucketId,
        bucketDisplayName = this.bucketInfo.bucketName,

        // image-specific (nullable in unified schema)
        orientationDeg = this.image?.orientationDeg,

        // video-specific (nullable in unified schema)
        durationMs = this.video?.durationMs,
        rotationDeg = this.video?.rotationDeg,

        // timeline
        createdAtMs = this.timeInfo.createdAtMs,
        modifiedAtMs = this.timeInfo.modifiedAtMs,
        generationModified = this.timeInfo.generationModified,

        // pipeline
        thumbnailPath = null,
        fingerprint = fingerPrint,
        processedState = ProcessedState.PENDING,
        lastProcessedAtMs = null,
        attemptCount = 0,
        lastErrorCode = null,
        lastErrorMessage = null,

        // lifecycle
        isDeleted = false,
        updatedAtMs = System.currentTimeMillis()
    )
}

fun MediaWithFacesEntity.toDomain(): MediaWithFacesDomain {
    return MediaWithFacesDomain(
        media = this.media.toDomain(),
        faces = faces.map {
            it.toDomain()
        }
    )
}

fun FaceEntity.toDomain(): FaceEmbedding {
    return FaceEmbedding(
        faceId = this.faceId,
        embedding = this.embeddingData,
    )
}

fun MediaEntity.toDomain(): MediaDomain {
    return MediaDomain(
        mediaId = this.mediaId,
        thumbnailUri = this.thumbnailPath,
        dateModified = this.modifiedAtMs,
        sourceStableId = this.sourceStableId,
        contentPath = this.contentPath
    )
}

