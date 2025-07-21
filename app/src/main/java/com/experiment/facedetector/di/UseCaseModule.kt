package com.experiment.facedetector.di

import com.experiment.facedetector.domain.usecase.ClearSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.GetSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.SaveSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.facesearch.ExtractEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchPhotosPagedUseCase
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
