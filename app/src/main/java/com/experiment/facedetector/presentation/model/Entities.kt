package com.experiment.facedetector.presentation.model

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import com.experiment.facedetector.domain.entities.FaceBoundingBox



data class MediaItemUi(
    val mediaId: Long,
    val thumbnailPath: String?,
    val contentPath: String?,
)

data class FaceSearchItemUi(
    val faceId: String,
    val faceBoundingBox: FaceBoundingBox,
    val faceBitmap: Bitmap,
)

