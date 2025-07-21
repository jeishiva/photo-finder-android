package com.experiment.facedetector.domain.usecase.facesearch

import android.graphics.Bitmap
import com.experiment.facedetector.domain.entities.FaceDetectedMediaItem
import com.experiment.facedetector.image.BitmapHelper
import kotlinx.coroutines.Deferred
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

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

    suspend operator fun invoke(mediaItem: FaceDetectedMediaItem): Map<String, FloatArray> {
        val interpreter = initialize()
        val faceEmbeddings = mutableMapOf<String, FloatArray>()
        mediaItem.faces.forEach { face ->
            val cropped = imageHelper.cropAndResizeFace(mediaItem.image, face.boundingBox, 112)
            if (cropped != null) {
                val embedding = getFaceEmbeddingWithSupport(cropped, interpreter)
                faceEmbeddings[face.trackingId.toString()] = embedding
            }
        }
        return faceEmbeddings
    }


    suspend operator fun invoke(bitmap: Bitmap): FloatArray {
        val interpreter = initialize()
        return getFaceEmbeddingWithSupport(bitmap, interpreter)
    }

    fun getFaceEmbeddingWithSupport(bitmap: Bitmap, interpreter: Interpreter): FloatArray {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 128f))
            .build()
        val processed = processor.process(tensorImage)
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 128), DataType.FLOAT32)
        interpreter.run(processed.buffer, outputBuffer.buffer.rewind())
        return outputBuffer.floatArray
    }
}
