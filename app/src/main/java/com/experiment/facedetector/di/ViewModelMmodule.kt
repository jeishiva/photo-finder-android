package com.experiment.facedetector.di


import androidx.lifecycle.SavedStateHandle
import com.experiment.facedetector.domain.usecase.GetSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.facesearch.AddFacesUseCase
import com.experiment.facedetector.domain.usecase.facesearch.GetAllEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchFaceUseCase
import com.experiment.facedetector.viewmodel.SearchViewModel
import com.experiment.facedetector.viewmodel.FullImageViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import com.experiment.facedetector.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        GalleryViewModel(
            mediaRepo = get(),
            workManager = get()
        )
    }
    viewModel {
        SplashViewModel()
    }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { (handle: SavedStateHandle) ->
        SearchViewModel(
            savedStateHandle = handle,
            getSearchQueryUseCase = get(),
            addFaceToGalleryUseCase = get(),
            getAllEmbeddingsUseCase = get(),
            searchFaceUseCase = get(),
            workManager = get(),
        )
    }
    viewModel { (handle: SavedStateHandle) ->
        FullImageViewModel(
            savedStateHandle = handle,
            faceDetectionProcessor = get(),
            imageHelper = get(),
            mediaRepo = get()
        )
    }
}
