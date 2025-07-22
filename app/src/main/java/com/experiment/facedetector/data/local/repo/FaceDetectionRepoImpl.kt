package com.experiment.facedetector.data.local.repo

import android.graphics.Bitmap
import com.experiment.facedetector.common.await
import com.experiment.facedetector.common.toFaceBoundingBox
import com.experiment.facedetector.config.FullImageConfig
import com.experiment.facedetector.data.local.entities.FaceDetectionResult
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

    companion object {
        // prevents 00M for large number of faces in the input
        private const val FACES_BATCH_SIZE = 10
    }

    override suspend fun detectFaces(localImageItem: LocalImageItem): FaceDetectionResult {
        return withContext(Dispatchers.Default) {
            val bitmap = decodeBitmap(localImageItem.uriString)
            val faces = detectFacesFromBitmap(bitmap)
            val faceDetectedItems = processFacesInBatches(faces, bitmap)
            FaceDetectionResult(faceDetectedItems)
        }
    }

    private fun decodeBitmap(uriString: String): Bitmap {
        return imageHelper.decodeBitmap(
            uriString,
            FullImageConfig.MAX_HEIGHT,
            FullImageConfig.MAX_WIDTH
        )
    }

    private suspend fun detectFacesFromBitmap(bitmap: Bitmap): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return faceDetector.process(inputImage).await()
    }

    private suspend fun processFacesInBatches(
        faces: List<Face>,
        bitmap: Bitmap
    ): List<FaceDetectedItem> {
        return coroutineScope {
            faces.chunked(FACES_BATCH_SIZE)
                .flatMap { batch ->
                    processFaceBatch(batch, bitmap)
                }
        }
    }

    private suspend fun processFaceBatch(
        faceBatch: List<Face>,
        bitmap: Bitmap
    ): List<FaceDetectedItem> = coroutineScope {
        faceBatch.map { face ->
            async(Dispatchers.Default) {
                createFaceDetectedItem(face, bitmap)
            }
        }.awaitAll().filterNotNull()
    }

    private fun createFaceDetectedItem(face: Face, bitmap: Bitmap): FaceDetectedItem? {
        val faceBoundingBox = face.toFaceBoundingBox()
        val faceBitmap = imageHelper.cropFaceFromBitmap(bitmap, faceBoundingBox)
            ?: return null
        return FaceDetectedItem(
            faceId = generateFaceId(face),
            faceBoundingBox = faceBoundingBox,
            faceBitmap = faceBitmap
        )
    }

    private fun generateFaceId(face: Face): String {
        return face.trackingId?.toString() ?: UUID.randomUUID().toString()
    }

}