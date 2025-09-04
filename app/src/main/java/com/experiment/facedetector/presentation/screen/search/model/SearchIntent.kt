package com.experiment.facedetector.presentation.screen.search.model

import com.experiment.facedetector.presentation.model.FaceSearchItemUi
import com.experiment.facedetector.presentation.model.MediaItemUi

sealed class SearchIntent {
    data class Start(val searchFaces: List<FaceSearchItemUi>) : SearchIntent()
    data class ImageSelected(val mediaItemUi: MediaItemUi) : SearchIntent()
    data object PhotoPreviewHandled : SearchIntent()
}