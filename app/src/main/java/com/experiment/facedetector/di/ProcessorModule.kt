package com.experiment.facedetector.di

import com.experiment.facedetector.data.local.worker.CameraImageProcessor
import com.experiment.facedetector.face.FaceDetectionProcessor
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.data.local.worker.processor.ICameraProcessor
import com.experiment.facedetector.data.local.worker.processor.IProcessor
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import org.koin.dsl.single
import kotlin.math.sin

val processorModule = module {

    single {
        FaceDetectionProcessor(
            faceDetector = get(),
            imageHelper = get()
        )
    }

    single<ICameraProcessor> {
        CameraImageProcessor(
            context = get(),
            faceDetectionProcessor = get(),
            embeddingsUseCase = get(),
            mediaRepo = get(),
            imageHelper = get()
        )
    }

    worker {
        CameraImageWorker(
            context = get(),
            workerParams = get(),
            processor = get()
        )
    }
}
