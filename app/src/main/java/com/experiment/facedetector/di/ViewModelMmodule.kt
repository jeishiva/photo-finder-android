package com.experiment.facedetector.di


import androidx.lifecycle.SavedStateHandle
import com.experiment.facedetector.viewmodel.SearchViewModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import com.experiment.facedetector.viewmodel.SelectPhotoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SplashViewModel()
    }
    viewModel { SelectPhotoViewModel(get()) }
    viewModel { (handle: SavedStateHandle) ->
        SearchViewModel(
            savedStateHandle = handle,
            mediaIndexerFactory = get(),
            embeddingUseCase = get(),
            searchPhotosPagedUseCase = get(),
        )
    }
}
