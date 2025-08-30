package com.experiment.facedetector.data.local.entities

import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.Media
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
    val fp = fingerPrint.generate(
        sourceStableId = this.sourceStableId.toString(),
        lastModified = this.lastModifiedAtMs,
        sizeBytes = this.sizeBytes
    )

    // Decide kind from presence of the nested payloads on the unified item
    val kind: MediaKind = if (this.video != null) {
        MediaKind.VIDEO
    } else {
        MediaKind.IMAGE
    }

    return MediaEntity(
        id = stableIdGenerator.generate(buildString {
            append(this@toMediaEntity.sourceKey)
            append("_")
            append(this@toMediaEntity.sourceStableId.toString())
        }),
        // identity
        sourceKey = sourceKey,
        sourceStableId = this.sourceStableId.toString(),
        contentUri = this.contentUri.toString(),
        mediaKind = kind, // new: IMAGE or VIDEO

        // descriptive
        mimeType = this.mimeType,
        width = this.width,
        height = this.height,
        sizeBytes = this.sizeBytes,
        bucketId = this.bucketId,
        bucketDisplayName = this.bucketDisplayName,

        // image-specific (nullable in unified schema)
        orientationDeg = this.image?.orientationDeg,

        // video-specific (nullable in unified schema)
        durationMs = this.video?.durationMs,
        rotationDeg = this.video?.rotationDeg,

        // timeline
        createdAtMs = this.createdAtMs,
        modifiedAtMs = this.lastModifiedAtMs,
        generationModified = this.generationModified,

        // pipeline
        thumbnailPath = null,
        fingerprint = fp,
        processedState = ProcessedState.PENDING,
        lastProcessedStage = null,
        lastProcessedAtMs = null,
        attemptCount = 0,
        lastErrorCode = null,
        lastErrorMessage = null,

        // lifecycle
        isDeleted = false,
        updatedAtMs = System.currentTimeMillis()
    )
}

fun MediaWithFaces.toDomain(): MediaWithFacesDomain {
    return MediaWithFacesDomain(
        media = Media(
            id = media.id,
            thumbnailUri = media.thumbnailPath,
            dateModified = media.modifiedAtMs,
            sourceStableId = media.sourceStableId
        ),
        faces = faces.map { it.toDomain() }
    )
}

fun FaceEntity.toDomain(): FaceEmbedding {
    return FaceEmbedding(
        id = this.faceId,
        embedding = this.embeddingData,
    )
}
