package com.experiment.facedetector.domain.entities

fun FaceDetectedItem.toFaceSearchItem(): FaceSearchItem {
    return FaceSearchItem(
        faceId = this.faceId,
        faceBoundingBox = this.faceBoundingBox,
        faceBitmap = this.faceBitmap
    )
}

