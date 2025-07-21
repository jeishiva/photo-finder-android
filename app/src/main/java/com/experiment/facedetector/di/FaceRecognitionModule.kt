package com.experiment.facedetector.di

import android.content.Context
import org.koin.dsl.module
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

val faceRecognitionModule = module {
    single {
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
        val context: Context = get()
        val modelBuffer = loadModelFile(context, "mobile_face_net.tflite")
        Interpreter(modelBuffer)
    }
}
