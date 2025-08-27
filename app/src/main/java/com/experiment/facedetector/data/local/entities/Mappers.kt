package com.experiment.facedetector.data.local.entities

import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.Media
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.source.MediaSourceType
import com.experiment.facedetector.domain.source.SourceMediaItem

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

fun MediaWithFaces.toDomain(): MediaWithFacesDomain {
    return MediaWithFacesDomain(
        media = Media(
            id = media.mediaId,
            thumbnailUri = media.thumbnailUri,
            dateModified = media.dateModified,
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
