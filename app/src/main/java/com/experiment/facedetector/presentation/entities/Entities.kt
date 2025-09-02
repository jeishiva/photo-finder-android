package com.experiment.facedetector.presentation.entities

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.experiment.facedetector.domain.entities.FaceBoundingBox
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.navigation.NavigationManager
import com.experiment.facedetector.viewmodel.SearchViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel

data class GalleryScreenParams(
    val navigationManager: NavigationManager,
    val viewModel: GalleryViewModel,
)

data class SearchScreenParams(
    val navigationManager: NavigationManager,
    val searchViewModel: SearchViewModel,
    val galleryViewModel: GalleryViewModel,
    val sessionId : String
)

data class AppUiModel(val actions: Actions) {
    @Stable
    data class Actions(
        val onPermissionGranted: () -> Unit = {},
    )
}

data class GalleryUiModel(
    val actions: Actions,
    val state: GalleryUiState,
) {
    @Stable
    data class Actions(
        val onImageSelected: (String?) -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val toggleFaceSelection: (String) -> Unit = {},
        val launchGalleryClicked: () -> Unit = {},
        val onFaceSelectionSheetShown: () -> Unit = {},
        val onThumbnailClicked: (MediaItemUi) -> Unit = {},
        val onRefreshClicked : () -> Unit = {},
    )
    val showSelectedFaces = state.showSelectedFaces
}

@Immutable
data class GalleryUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val selectedImagePath: String? = null,
    val showSelectedFaces: Boolean = false,
    var navigateToSearch: Boolean = false,
    var sessionId: String? = null,
    val faceList: List<FaceDetectedItem> = emptyList(),
    val showRefreshButton: Boolean = false,
)

@Immutable
data class SearchUiState(
    val isEmptySearchResult: Boolean = false,
    val faceList: List<FaceSearchItemUi> = emptyList(),
    val previewPhotoPath: String? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = "",
)

data class SearchUiModel(
    val actions: Actions,
    val state: SearchUiState,
) {
    @Stable
    data class Actions(
        val onBackClick: () -> Unit = {},
        val onThumbnailClicked: (MediaItemUi) -> Unit = {},
        val onPhotoPreviewDismissed: () -> Unit = {},
        val onShareClicked: (contentPath: String) -> Unit = {},
    )
}

data class MediaItemUi(
    val mediaId: Long,
    val thumbnailPath: String?,
    val contentPath: String?,
)

data class FaceSearchItemUi(
    val faceId: String,
    val faceBoundingBox: FaceBoundingBox,
    val faceBitmap: Bitmap,
)

