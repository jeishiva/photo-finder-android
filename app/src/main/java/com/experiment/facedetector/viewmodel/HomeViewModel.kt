package com.experiment.facedetector.viewmodel

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.LocalImageItem
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.ui.TimeRange
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(val faceDetectionUseCase: FaceDetectionUseCase) : ViewModel() {

    private val _uiState = UiStateHolder<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.state

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Search -> {
                detectFaces(
                    intent.selectedImage,
                    intent.selectedOption
                )
            }
        }
    }

    fun detectFaces(selectedImage: Uri, selectedTimeRange: TimeRange) {
        LogManager.d("HomeViewModel", "selected image: $selectedImage")
        viewModelScope.launch(Dispatchers.IO) {
            validationStart()
            val result =
                faceDetectionUseCase(localImageItem = LocalImageItem(selectedImage.toString()))
            if (result.faces.isEmpty()) {
                facesNotFound()
            } else {
                sendDetectedFaces(result.faces)
            }
            LogManager.d("HomeViewModel", "total faces: ${result.faces.size}")
        }
    }

    fun validationStart() {
        _uiState.setState {
            HomeUiState(isLoading = true)
        }
    }

    fun facesNotFound() {
        _uiState.setState {
            copy(
                errorMessage = "Faces not found, Select another image with faces",
                isLoading = false
            )
        }
    }

    fun sendDetectedFaces(faces: List<FaceDetectedItem>) {
        _uiState.setState {
            copy(
                list = faces,
                isLoading = false,
                message = "${faces.size} faces found"
            )
        }
    }

    fun reset() {
        _uiState.setState {
            HomeUiState()
        }
    }
}

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = "",
    val list: List<FaceDetectedItem> = emptyList()
)

sealed class HomeIntent {
    data class Search(val selectedImage: Uri, val selectedOption: TimeRange) : HomeIntent()
}