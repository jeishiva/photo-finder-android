package com.experiment.facedetector.face

import com.experiment.facedetector.common.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.experiment.facedetector.domain.entities.FaceDetectedMediaItem
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import android.graphics.Bitmap
import com.experiment.facedetector.config.FullImageConfig
import com.experiment.facedetector.domain.source.SourceMediaItem
import com.experiment.facedetector.image.BitmapHelper

class FaceDetectionProcessor(
    private val faceDetector: FaceDetector,
    private val imageHelper: BitmapHelper
) {
    suspend fun processImage(mediaItem: SourceMediaItem): FaceDetectedMediaItem =
        withContext(Dispatchers.IO) {
            val bitmap = imageHelper.decodeBitmap(
                mediaItem.contentUri,
                FullImageConfig.MAX_HEIGHT,
                FullImageConfig.MAX_WIDTH
            )
            val faces = detectFaces(bitmap)
            FaceDetectedMediaItem(mediaItem, faces, bitmap)
        }

    suspend fun detectFaces(bitmap: Bitmap): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return faceDetector.process(inputImage).await()
    }
}
