package com.experiment.facedetector.di

import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory<FaceDetectionUseCase> {
        FaceDetectionUseCase(faceDetectionRepo = get())
    }
}
