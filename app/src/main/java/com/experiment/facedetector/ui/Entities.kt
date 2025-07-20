package com.experiment.facedetector.ui

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.viewmodel.HomeViewModel
import com.experiment.facedetector.viewmodel.SearchViewModel

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
    val viewModel: HomeViewModel,
)

data class SearchScreenParams(
    val navController: NavHostController,
    val searchViewModel: SearchViewModel,
)

data class HomeUiModel(
    val selectedOption: TimeRange = TimeRange.OneMonth,
    val actions: Actions,
    val state: HomeUiState
) {
    @Stable
    data class Actions(
        val onBackClick: () -> Unit = {},
        val onImageSelected: (Uri?) -> Unit = {},
        val onOptionSelected: (TimeRange) -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val isFaceSelected: (String) -> Boolean = { false },
        val toggleFaceSelection: (String) -> Unit = {},
    )

    val hasFaces
        get() = state.faceList.isNotEmpty()

    val hasSelectedFaces
        get() = state.hasSelectedFaces

}

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = "",
    val faceList: List<FaceDetectedItem> = emptyList(),
    val selectedImageUri : Uri? = null,
    val hasSelectedFaces: Boolean = false,
    val searchSessionId: String? = null
)

@Immutable
data class SearchUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = "",
    val faceList: List<FaceDetectedItem> = emptyList(),
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


