package com.experiment.facedetector.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.LocalImageItem
import com.experiment.facedetector.domain.usecase.ClearSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.SaveSearchQueryUseCase
import com.experiment.facedetector.ui.HomeUiState
import com.experiment.facedetector.ui.TimeRange
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class HomeViewModel(
    val faceDetectionUseCase: FaceDetectionUseCase,
    val saveFacesUseCase: SaveSearchQueryUseCase,
    val clearFacesUseCase: ClearSearchQueryUseCase
) : ViewModel() {

    private val _uiState = UiStateHolder<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.state

    val selectedFaceMap = mutableStateMapOf<String, Boolean>()

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Search -> {
                detectFaces(
                    intent.selectedImage, intent.selectedOption
                )
            }
        }
    }

    fun setSelectedImage(selectedImageUri: Uri?) {
        _uiState.setState {
            copy(selectedImageUri = selectedImageUri)
        }
    }

    fun detectFaces(selectedImage: Uri, selectedTimeRange: TimeRange) {
        LogManager.d("HomeViewModel", "selected image: $selectedImage")
        viewModelScope.launch(Dispatchers.IO) {
            startFaceDetection()
            val result =
                faceDetectionUseCase(localImageItem = LocalImageItem(selectedImage.toString()))
            if (result.faces.isEmpty()) {
                facesNotFound()
                return@launch
            }
            sendDetectedFaces(result.faces)
            LogManager.d("HomeViewModel", "total faces: ${result.faces.size}")
        }
    }

    fun startFaceDetection() {
        _uiState.setState {
            val selectedImageUri = uiState.value.selectedImageUri
            HomeUiState(isLoading = true, selectedImageUri = selectedImageUri)
        }
    }

    fun getSelectedFaces(): List<FaceDetectedItem> {
        val faceList = uiState.value.faceList
        LogManager.d("HomeViewModel", "total faces: ${faceList.size}")
        LogManager.d("HomeViewModel", "selected faces: ${selectedFaceMap.size}")
        return uiState.value.faceList.filter {
            selectedFaceMap.containsKey(it.faceId)
        }
    }

    fun saveSelectedFaces() {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedFaces = getSelectedFaces()
            _uiState.setState {
                copy(
                    isLoading = true,
                    message = "Saving faces..."
                )
            }
            clearFacesUseCase()
            val sessionId = saveFacesUseCase(selectedFaces)
            _uiState.setState {
                copy(
                    isLoading = false,
                    message = "",
                    searchSessionId = sessionId
                )
            }
        }
    }

    fun resetSessionId() {
        _uiState.setState {
            copy(
                searchSessionId = null
            )
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
                faceList = faces, isLoading = false, message = "${faces.size} faces found"
            )
        }
    }

    private fun clearSelection() {
        selectedFaceMap.clear()
    }

    fun reset() {
        _uiState.setState {
            HomeUiState()
        }
    }

    fun toggleFaceSelection(faceId: String) {
        when {
            selectedFaceMap.containsKey(faceId) -> {
                selectedFaceMap.remove(faceId)
            }

            selectedFaceMap.size < MAX_SELECTED_FACES -> {
                selectedFaceMap[faceId] = true
            }
        }
        _uiState.setState {
            copy(
                hasSelectedFaces = selectedFaceMap.isNotEmpty()
            )
        }
    }

    fun isFaceSelected(faceId: String): Boolean {
        return selectedFaceMap.containsKey(faceId)
    }

    companion object {
        private const val MAX_SELECTED_FACES = 3
    }
}


sealed class HomeIntent {
    data class Search(val selectedImage: Uri, val selectedOption: TimeRange) : HomeIntent()
}