package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap

data class LocalImageItem(
    val uriString: String
)

data class FaceBoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class FaceDetectedItem(
    val faceId: String,
    val faceBoundingBox: FaceBoundingBox,
    val faceBitmap: Bitmap
)