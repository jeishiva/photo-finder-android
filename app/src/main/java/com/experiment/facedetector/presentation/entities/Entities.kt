package com.experiment.facedetector.presentation.entities

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import com.experiment.facedetector.domain.entities.FaceBoundingBox
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.viewmodel.SearchViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel

data class GalleryScreenParams(
    val navController: NavHostController,
    val viewModel: GalleryViewModel,
)

data class SearchScreenParams(
    val navController: NavHostController,
    val searchViewModel: SearchViewModel,
    val selectPhotoViewModel: GalleryViewModel,
)


data class AppUiModel(
    val actions: Actions,
) {
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
        val onImageSelected: (Uri?) -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val toggleFaceSelection: (String) -> Unit = {},
        val launchGalleryClicked: () -> Unit = {},
        val onFaceSelectionSheetShown: () -> Unit = {},
        val onThumbnailClicked: (MediaItemUi) -> Unit = {},
    )
    val showSelectedFaces = state.showSelectedFaces
}

@Immutable
data class GalleryUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val selectedImageUri: Uri? = null,
    val showSelectedFaces: Boolean = false,
    var navigateToSearch: Boolean = false,
    var sessionId: String? = null,
    val faceList: List<FaceDetectedItem> = emptyList(),
)

@Immutable
data class SearchUiState(
    val isLoading: Boolean = false,
    val isEmptySearchResult: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = "",
    val faceList: List<FaceSearchItemUi> = emptyList(),
)

data class SearchUiModel(
    val actions: Actions,
    val state: SearchUiState,
) {
    @Stable
    data class Actions(
        val onBackClick: () -> Unit = {},
        val onThumbnailClicked: (MediaItemUi) -> Unit = {},
    )
}

data class MediaItemUi(
    val mediaId: Long,
    val thumbnailUri: String?,
)

data class FaceSearchItemUi(
    val faceId: String,
    val faceBoundingBox: FaceBoundingBox,
    val faceBitmap: Bitmap,
)

