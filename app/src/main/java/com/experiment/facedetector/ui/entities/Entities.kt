package com.experiment.facedetector.ui.entities

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.viewmodel.SearchViewModel
import com.experiment.facedetector.viewmodel.SelectPhotoViewModel

sealed class TimeRange(val label: String) {
    object OneMonth : TimeRange("1 Month")
    object ThreeMonths : TimeRange("3 Months")
    object SixMonths : TimeRange("6 Months")
    object TwelveMonths : TimeRange("12 Months")

    companion object {
        fun toList(): List<TimeRange> = listOf(
            OneMonth,
            ThreeMonths,
            SixMonths,
            TwelveMonths
        )
    }
}

data class HomeScreenParams(
    val navController: NavHostController,
    val viewModel: SelectPhotoViewModel,
)

data class SearchScreenParams(
    val navController: NavHostController,
    val searchViewModel: SearchViewModel,
    val selectPhotoViewModel: SelectPhotoViewModel
)

data class HomeUiModel(
    val selectedOption: TimeRange = TimeRange.OneMonth,
    val actions: Actions,
    val state: HomeUiState
) {
    @Stable
    data class Actions(
        val onImageSelected: (Uri?) -> Unit = {},
        val onOptionSelected: (TimeRange) -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val toggleFaceSelection: (String) -> Unit = {},
        val launchGalleryClicked: () -> Unit = {},
        val onFaceSelectionSheetShown: () -> Unit = {},
        val onThumbnailClicked: () -> Unit = {},
        )
    val showSelectedFaces = state.showSelectedFaces
}

@Immutable
data class HomeUiState(
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
    val faceList: List<FaceSearchItem> = emptyList(),
)

data class SearchUiModel(
    val actions: Actions,
    val state: SearchUiState
) {
    @Stable
    data class Actions(
        val onBackClick: () -> Unit = {},
    )
}

data class MediaWithFacesUi(
    val id: Long,
    val thumbnailUri: String?
)