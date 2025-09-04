package com.experiment.facedetector.presentation.screen.gallery.model

import androidx.compose.runtime.Stable
import com.experiment.facedetector.presentation.model.MediaItemUi

@Stable
data class GalleryActions(
    val onExternalImageSelected: (String?) -> Unit = {},
    val onSearchClick: () -> Unit = {},
    val toggleFaceSelection: (String) -> Unit = {},
    val launchGalleryClicked: () -> Unit = {},
    val onFaceSelectionSheetShown: () -> Unit = {},
    val onThumbnailClicked: (MediaItemUi) -> Unit = {},
    val onRefreshClicked : () -> Unit = {},
)