package com.experiment.facedetector.presentation.screen.gallery.model

sealed class GalleryNavigationEvent {
    data class ToSearch(val sessionId : String) : GalleryNavigationEvent()
}

