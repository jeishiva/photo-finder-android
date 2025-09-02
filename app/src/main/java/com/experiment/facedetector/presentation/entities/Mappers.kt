package com.experiment.facedetector.presentation.entities

import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain

fun MediaWithFacesDomain.toUi(): MediaItemUi {
    return MediaItemUi(
        mediaId = media.mediaId,
        thumbnailPath = media.thumbnailUri,
        contentPath = media.contentPath
    )
}

fun FaceDetectedItem.toFaceSearchItem(): FaceSearchItemUi {
    return FaceSearchItemUi(
        faceId = this.faceId,
        faceBoundingBox = this.faceBoundingBox,
        faceBitmap = this.faceBitmap
    )
}
