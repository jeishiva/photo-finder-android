package com.experiment.facedetector.presentation.screen.gallery.model

import com.experiment.facedetector.navigation.NavigationManager
import com.experiment.facedetector.viewmodel.GalleryViewModel

data class GalleryScreenArgs(
    val navigationManager: NavigationManager,
    val viewModel: GalleryViewModel,
)