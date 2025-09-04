package com.experiment.facedetector.di


import androidx.lifecycle.SavedStateHandle
import com.experiment.facedetector.viewmodel.AppViewModel
import com.experiment.facedetector.viewmodel.SearchViewModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SplashViewModel()
    }
    viewModel {
        GalleryViewModel(
            faceDetectionUseCase = get(),
            getSyncedMediaUseCase = get(),
            invalidationRepository = get(),
        )
    }
    viewModel { (handle: SavedStateHandle) ->
        SearchViewModel(
            savedStateHandle = handle,
            embeddingUseCase = get(),
            searchPhotosPagedUseCase = get(),
        )
    }

    viewModel {
        AppViewModel(
            scanMediaUseCase = get(),
            workScheduler = get()
        )
    }

}
