package com.experiment.facedetector.di


import androidx.lifecycle.SavedStateHandle
import com.experiment.facedetector.viewmodel.SearchViewModel
import com.experiment.facedetector.viewmodel.FullImageViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import com.experiment.facedetector.viewmodel.SelectPhotoViewModel
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
    viewModel { SelectPhotoViewModel(get()) }
    viewModel { (handle: SavedStateHandle) ->
        SearchViewModel(
            savedStateHandle = handle,
            searchFaceUseCase = get(),
            embeddingUseCase = get(),
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
