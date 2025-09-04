package com.experiment.facedetector.presentation.screen.gallery.model

sealed class GalleryIntent {
    data object ShowDetectedFaces : GalleryIntent()
    data class ImageSelected(val contentPath : String) : GalleryIntent()
    data object GalleryRefreshed : GalleryIntent()
    data object ResetImageSelection: GalleryIntent()
    data object LaunchSearch : GalleryIntent()
}