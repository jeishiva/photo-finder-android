package com.experiment.facedetector.di

import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.facesearch.ExtractEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchPhotosPagedUseCase
import org.koin.dsl.module

val useCaseModule = module {

    factory<FaceDetectionUseCase> {
        FaceDetectionUseCase(faceDetectionRepo = get())
    }

    factory<SearchPhotosPagedUseCase> {
        SearchPhotosPagedUseCase(repository = get())
    }

    factory<ExtractEmbeddingsUseCase> {
        ExtractEmbeddingsUseCase(
            interpreterDeferred = get(),
            imageHelper = get()
        )
    }
}
