package com.experiment.facedetector.data.local.worker.entities

import android.graphics.Bitmap
import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.MediaItem
import com.google.mlkit.vision.face.Face

data class BatchResult(val processed: Int, val saved: Int)

data class ProcessedImageResult(
    val mediaItem: MediaItem,
    val thumbnailBitmap: Bitmap,
    val faceEmbeddings: List<FaceEmbedding>,
    val faces: List<Face>,
    val originalBitmap: Bitmap
)
