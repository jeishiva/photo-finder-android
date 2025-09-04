package com.experiment.facedetector.di

import android.content.Context
import com.experiment.facedetector.domain.processing.FaceEmbeddingExtractor
import com.experiment.facedetector.data.processing.TensorFlowFaceEmbeddingExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.koin.dsl.module
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

val faceRecognitionModule = module {
    single<FaceEmbeddingExtractor> {
        TensorFlowFaceEmbeddingExtractor(get())
    }
    single<Deferred<Interpreter>> {
        provideInterpreterAsync(get())
    }
}

fun provideInterpreterAsync(context: Context): Deferred<Interpreter> {
    return CoroutineScope(Dispatchers.IO).async {
        fun loadModelFile(context: Context, modelFileName: String): MappedByteBuffer {
            val fileDescriptor = context.assets.openFd(modelFileName)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }

        val modelBuffer = loadModelFile(context, "mobile_face_net.tflite")
        Interpreter(modelBuffer)
    }
}
