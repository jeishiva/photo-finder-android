package com.experiment.facedetector.di

import ExtractEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.GetSyncedMediaUseCase
import com.experiment.facedetector.domain.usecase.ScanMediaUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchSimilarPhotoUseCase
import org.koin.dsl.module

val useCaseModule = module {

    factory<ScanMediaUseCase> {
        ScanMediaUseCase(
            mediaScanner = get()
        )
    }

    factory<GetSyncedMediaUseCase> {
        GetSyncedMediaUseCase(
            mediaRepository = get()
        )
    }

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

    factory<SearchSimilarPhotoUseCase> {
        SearchSimilarPhotoUseCase(
            mediaWithFacesRepository = get(),
            faceEmbeddingMatcher = get()
        )
    }
}
