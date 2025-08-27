package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face
/**
 *  faces detected in image
 */
data class FaceDetectedMediaItem(
    val mediaItem: SourceMediaItem,
    val faces: List<Face>,
    val image: Bitmap,
)

data class FaceEmbeddingRequest(
    val faces: List<Face>,
    val image: Bitmap,
)

