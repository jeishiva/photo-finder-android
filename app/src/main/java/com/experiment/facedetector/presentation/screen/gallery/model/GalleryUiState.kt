package com.experiment.facedetector.presentation.screen.gallery.model

import androidx.compose.runtime.Immutable
import com.experiment.facedetector.domain.entities.FaceDetectedItem

@Immutable
data class GalleryUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val faceExtractionState: FaceExtractionState = FaceExtractionState(),
    val showRefreshHint: Boolean = false,
)

data class FaceExtractionState(
    val showSelectedFaces: Boolean = false,
    val selectedImagePath: String? = null,
    val faceList: List<FaceDetectedItem> = emptyList(),
    val isInProgress: Boolean = false,
    val showFaceSelectionSheet: Boolean = false,
) {
    val hasFaces: Boolean get() = faceList.isNotEmpty()
    val isCompleted: Boolean get() = !isInProgress
    val canShowFaces = isCompleted && hasFaces
    val isFaceNotFound = isCompleted && !hasFaces
}