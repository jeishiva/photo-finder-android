package com.experiment.facedetector.data.local.repo

import android.graphics.Bitmap
import com.experiment.facedetector.common.await
import com.experiment.facedetector.common.toFaceId
import com.experiment.facedetector.config.FullImageConfig
import com.experiment.facedetector.data.local.entities.FaceDetectionResult
import com.experiment.facedetector.domain.entities.FaceBoundingBox
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.LocalImageItem
import com.experiment.facedetector.domain.repo.FaceDetectionRepo
import com.experiment.facedetector.image.BitmapHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.UUID

class FaceDetectionRepoImpl(
    private val faceDetector: FaceDetector,
    private val imageHelper: BitmapHelper
) : FaceDetectionRepo {
    override suspend fun detectFaces(localImageItem: LocalImageItem): FaceDetectionResult {
        return withContext(Dispatchers.Default) {
            val bitmap = imageHelper.decodeBitmap(
                localImageItem.uriString,
                FullImageConfig.MAX_HEIGHT,
                FullImageConfig.MAX_WIDTH
            )
            // we are limiting to 5 faces per image to avoid OOM in case of large images with many faces
            val faceDetectedItems = coroutineScope {
                detectFaces(bitmap)
                    .chunked(5)
                    .flatMap { batch ->
                        batch.map { face ->
                            async(Dispatchers.Default) {
                                val faceBoundingBox = FaceBoundingBox(
                                    left = face.boundingBox.left,
                                    top = face.boundingBox.top,
                                    right = face.boundingBox.right,
                                    bottom = face.boundingBox.bottom,
                                )
                                FaceDetectedItem(
                                    faceId = face.trackingId?.toString() ?: UUID.randomUUID()
                                        .toString(),
                                    faceBoundingBox = faceBoundingBox,
                                    faceBitmap = imageHelper.cropFaceFromBitmap(
                                        bitmap,
                                        faceBoundingBox
                                    )
                                )
                            }
                        }.awaitAll()
                    }
            }
            FaceDetectionResult(faceDetectedItems)
        }
    }

    suspend fun detectFaces(bitmap: Bitmap): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return faceDetector.process(inputImage).await()
    }
}
