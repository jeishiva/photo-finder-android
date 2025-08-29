package com.experiment.facedetector.presentation.entities

import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain

fun MediaWithFacesDomain.toUi(): MediaItemUi {
    return MediaItemUi(
        id = media.id,
        thumbnailUri = media.thumbnailUri,
    )
}

fun FaceDetectedItem.toFaceSearchItem(): FaceSearchItemUi {
    return FaceSearchItemUi(
        faceId = this.faceId,
        faceBoundingBox = this.faceBoundingBox,
        faceBitmap = this.faceBitmap
    )
}
