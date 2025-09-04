package com.experiment.facedetector.presentation.screen.search.model

import androidx.compose.runtime.Stable
import com.experiment.facedetector.presentation.model.MediaItemUi

@Stable
data class SearchActions(
    val onBackClick: () -> Unit = {},
    val onThumbnailClicked: (MediaItemUi) -> Unit = {},
    val onPhotoPreviewDismissed: () -> Unit = {},
    val onShareClicked: (contentPath: String) -> Unit = {},
)