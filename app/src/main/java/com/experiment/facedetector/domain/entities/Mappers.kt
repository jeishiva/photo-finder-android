package com.experiment.facedetector.domain.entities

import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.repo.StableIdGenerator
import com.experiment.facedetector.domain.source.MediaSourceType
import com.experiment.facedetector.domain.source.SourceMediaItem


fun FaceDetectedItem.toFaceSearchItem(): FaceSearchItem {
    return FaceSearchItem(
        faceId = this.faceId,
        faceBoundingBox = this.faceBoundingBox,
        faceBitmap = this.faceBitmap
    )
}

fun SourceMediaItem.toMediaEntity(
    sourceType: MediaSourceType,
    fingerprint: MediaFingerPrint,
): MediaEntity {
    val fp = fingerprint.generate(
        sourceStableId = this.stableId.toString(),
        lastModified = this.lastModifiedTime,
        sizeBytes = this.fileSize
    )
    return MediaEntity(
        mediaId = 0L,
        contentUri = this.contentUri.toString(),
        thumbnailUri = null,
        source = sourceType.id,
        sourceStableId = this.stableId.toString(),
        dateModified = this.lastModifiedTime,
        sizeBytes = this.fileSize,
        fingerprint = fp
    )
}
