package com.experiment.facedetector.data.processing

import android.graphics.Bitmap
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.domain.processing.FaceEmbeddingExtractor
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

/**
 * TensorFlow Lite implementation of face embedding extraction
 */
class TensorFlowFaceEmbeddingExtractor(
    private val interpreterDeferred: Deferred<Interpreter>
) : FaceEmbeddingExtractor {

    @Volatile
    private var interpreterInstance: Interpreter? = null

    private val initializationMutex = Mutex()

    private val imageProcessor by lazy {
        ImageProcessor.Builder()
            .add(ResizeOp(
                FaceEmbeddingExtractor.INPUT_HEIGHT,
                FaceEmbeddingExtractor.INPUT_WIDTH,
                ResizeOp.ResizeMethod.BILINEAR
            ))
            .add(NormalizeOp(NORMALIZATION_MEAN, NORMALIZATION_STD))
            .build()
    }

    override suspend fun initialize() {
        if (interpreterInstance != null) {
            return
        }
        initializationMutex.withLock {
            if (interpreterInstance == null) {
                interpreterInstance = interpreterDeferred.await()
                LogManager.d(TAG, "TensorFlow Lite interpreter initialized")
            }
        }
    }

    override suspend fun extractEmbedding(faceBitmap: Bitmap): FloatArray {
        val interpreter = interpreterInstance
            ?: throw IllegalStateException("Extractor not initialized. Call initialize() first.")

        validateInputBitmap(faceBitmap)

        val tensorImage = TensorImage(DataType.FLOAT32).apply { load(faceBitmap) }
        val processedImage = imageProcessor.process(tensorImage)

        LogManager.v(TAG, "Processing face image: ${processedImage.height}x${processedImage.width}")

        val outputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(BATCH_SIZE, FaceEmbeddingExtractor.EMBEDDING_SIZE),
            DataType.FLOAT32
        )

        interpreter.run(processedImage.buffer, outputBuffer.buffer.rewind())

        return outputBuffer.floatArray.also { embedding ->
            LogManager.v(TAG, "Generated TensorFlow Lite embedding with ${embedding.size} dimensions")
        }
    }

    override fun cleanup() {
        interpreterInstance?.close()
        interpreterInstance = null
        LogManager.d(TAG, "TensorFlow Lite resources cleaned up")
    }

    private fun validateInputBitmap(bitmap: Bitmap) {
        require(!bitmap.isRecycled) { "Input bitmap is recycled" }
        require(bitmap.width > 0 && bitmap.height > 0) { "Invalid bitmap dimensions" }
    }

    companion object {
        private const val TAG = "TFLiteFaceEmbeddingExtractor"
        private const val BATCH_SIZE = 1
        private const val NORMALIZATION_MEAN = 127.5f
        private const val NORMALIZATION_STD = 128f
    }
}



