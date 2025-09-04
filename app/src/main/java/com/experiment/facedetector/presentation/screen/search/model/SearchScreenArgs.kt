package com.experiment.facedetector.presentation.screen.search.model

import com.experiment.facedetector.navigation.NavigationManager
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SearchViewModel

data class SearchScreenArgs(
    val navigationManager: NavigationManager,
    val searchViewModel: SearchViewModel,
    val galleryViewModel: GalleryViewModel,
    val sessionId : String
)