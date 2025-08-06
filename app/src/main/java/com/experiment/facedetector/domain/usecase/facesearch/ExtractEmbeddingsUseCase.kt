package com.experiment.facedetector.domain.usecase.facesearch

import android.graphics.Bitmap
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.common.toFaceBoundingBox
import com.experiment.facedetector.domain.entities.FaceEmbeddingRequest
import com.experiment.facedetector.image.BitmapHelper
import kotlinx.coroutines.Deferred
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.UUID

class ExtractEmbeddingsUseCase(
    private val interpreterDeferred: Deferred<Interpreter>,
    private val imageHelper: BitmapHelper,
) {
    private var interpreterInstance: Interpreter? = null

    private suspend fun initialize(): Interpreter {
        return interpreterInstance ?: interpreterDeferred.await().also {
            interpreterInstance = it
        }
    }

    suspend operator fun invoke(faceEmbeddingRequest: FaceEmbeddingRequest): List<Pair<String, FloatArray>> {
        val interpreter = initialize()
        return faceEmbeddingRequest.faces.mapNotNull { face ->
            val cropped = imageHelper.cropFaceFromBitmap(
                faceEmbeddingRequest.image,
                face.toFaceBoundingBox(),
            )
            if (cropped != null) {
                val embedding = getFaceEmbeddingWithSupport(cropped, interpreter)
                UUID.randomUUID().toString() to embedding
            } else {
                null
            }
        }
    }

    suspend operator fun invoke(imagePath: String): FloatArray? {
        val interpreter = initialize()
        return try {
            imageHelper.loadBitmapFromPath(imagePath)?.let { faceBitmap ->
                getFaceEmbeddingWithSupport(faceBitmap, interpreter)
            }
        } catch (ex: Exception) {
            null
        }
    }

    suspend operator fun invoke(faceBitmap: Bitmap): FloatArray? {
        val interpreter = initialize()
        return try {
            getFaceEmbeddingWithSupport(faceBitmap, interpreter)
        } catch (e: Exception) {
            null
        }
    }

    fun getFaceEmbeddingWithSupport(faceBitmap: Bitmap, interpreter: Interpreter): FloatArray {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(faceBitmap)
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 128f))
            .build()
        val processed = processor.process(tensorImage)
        LogManager.d(TAG, "embedding faceBitmap size : ${processed.height} x ${processed.width}")
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 128), DataType.FLOAT32)
        interpreter.run(processed.buffer, outputBuffer.buffer.rewind())
        return outputBuffer.floatArray
    }

    companion object {
        private const val TAG = "ExtractEmbeddingsUseCase"
    }
}
