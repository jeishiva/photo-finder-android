package com.experiment.facedetector.di


import androidx.lifecycle.SavedStateHandle
import com.experiment.facedetector.navigation.NavigationScope
import com.experiment.facedetector.viewmodel.DetailsViewModel
import com.experiment.facedetector.viewmodel.FullImageViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import com.experiment.facedetector.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { GalleryViewModel(
        mediaRepo = get(),
        workManager = get()
    ) }
    viewModel {
        SplashViewModel()
    }
    scope(named(NavigationScope.Home.name)) {
        viewModel { HomeViewModel(get()) }
    }
    viewModel {
        DetailsViewModel()
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
