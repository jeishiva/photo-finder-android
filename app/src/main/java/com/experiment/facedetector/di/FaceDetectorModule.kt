package com.experiment.facedetector.di

import com.experiment.facedetector.core.face.FaceDetectionProcessor
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.koin.dsl.module

val faceDetectorModule = module {
    single {
        provideFaceDetector()
    }
    single {
        FaceDetectionProcessor(
            faceDetector = get(),
            imageHelper = get()
        )
    }
}

private fun provideFaceDetector(): FaceDetector {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    return FaceDetection.getClient(options)
}
