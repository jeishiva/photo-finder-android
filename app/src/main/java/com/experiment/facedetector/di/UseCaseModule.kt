package com.experiment.facedetector.di

import com.experiment.facedetector.domain.usecase.ClearSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.GetSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.SaveSearchQueryUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory<FaceDetectionUseCase> {
        FaceDetectionUseCase(faceDetectionRepo = get())
    }
    factory<ClearSearchQueryUseCase> {
        ClearSearchQueryUseCase(searchQueryRepo = get())
    }
    factory<SaveSearchQueryUseCase> {
        SaveSearchQueryUseCase(searchQueryRepo = get())
    }
    factory<GetSearchQueryUseCase> {
        GetSearchQueryUseCase(searchQueryRepo = get())
    }
}
