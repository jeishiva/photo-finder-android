package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap
import android.net.Uri
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.domain.source.SourceMediaItem
import com.google.mlkit.vision.face.Face
import java.io.File

/**
 *  from gallery
 */
data class MediaItem(
    val mediaId: Long,
    val contentUri: Uri,
)

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

