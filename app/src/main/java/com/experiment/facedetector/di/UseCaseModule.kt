package com.experiment.facedetector.di

import ExtractEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchPhotosPagedUseCase
import org.koin.dsl.module

val useCaseModule = module {

    factory<FaceDetectionUseCase> {
        FaceDetectionUseCase(
            faceDetectionRepo = get()
        )
    }

    factory<ExtractEmbeddingsUseCase> {
        ExtractEmbeddingsUseCase(
            embeddingExtractor = get(),
            imageHelper = get()
        )
    }

    factory<SearchPhotosPagedUseCase> {
        SearchPhotosPagedUseCase(
            mediaPagingRepository = get(),
        )
    }

}
