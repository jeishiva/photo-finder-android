package com.experiment.facedetector.presentation.screen.search.model

import androidx.compose.runtime.Immutable
import com.experiment.facedetector.presentation.model.FaceSearchItemUi

@Immutable
data class SearchUiState(
    val isEmptySearchResult: Boolean = false,
    val faceList: List<FaceSearchItemUi> = emptyList(),
    val previewPhotoPath: String? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = "",
)
