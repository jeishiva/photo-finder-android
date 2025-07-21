package com.experiment.facedetector.data.local.repo

import android.graphics.Bitmap
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.domain.repo.FaceSearchRepository
import com.experiment.facedetector.image.BitmapHelper
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.sqrt

class FaceSearchRepositoryImpl(
    private val modelInterpreter: Interpreter,
    private val imageHelper: BitmapHelper,
) : FaceSearchRepository {

    private val faceEmbeddings = mutableListOf<FaceEmbedding>()

    override suspend fun addFaces(faces: List<FaceSearchItem>) {
        faces.forEach { item ->
            val bitmap = imageHelper.loadBitmapFromPath(item.thumbnailPath)
            if (bitmap != null) {
                val embedding = getFaceEmbeddingWithSupport(bitmap, modelInterpreter)
                LogManager.d("FaceSearchRepository", "Embedding: $embedding")
                faceEmbeddings.add(FaceEmbedding(id = item.faceId, embedding = embedding))
            } else {
                LogManager.e("FaceSearchRepository", "Failed to load bitmap from path: ${item.thumbnailPath}")
            }
        }
    }

    override suspend fun getAllEmbeddings(): List<FaceEmbedding> = faceEmbeddings

    override suspend fun searchFace(targetEmbedding: FloatArray, threshold: Float): List<FaceEmbedding> {
        return faceEmbeddings.filter { stored ->
            cosineSimilarity(targetEmbedding, stored.embedding) >= threshold
        }
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

    fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dot = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in vec1.indices) {
            dot += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }
        return dot / (sqrt(norm1) * sqrt(norm2))
    }

}
