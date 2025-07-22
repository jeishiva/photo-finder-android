package com.experiment.facedetector.viewmodel

import android.net.Uri
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    val faceDetectionUseCase: FaceDetectionUseCase,
    val saveFacesUseCase: SaveSearchQueryUseCase,
    val clearFacesUseCase: ClearSearchQueryUseCase
) : ViewModel() {

    private val _uiState = UiStateHolder<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.state

    private val _selectedFaceIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFaceIds: StateFlow<Set<String>> = _selectedFaceIds

    var activeSessionId: String? = null
        private set

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
        LogManager.d("HomeViewModel", "selected image: $selectedImage  $this")
        viewModelScope.launch(Dispatchers.IO) {
            clearSelection()
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
            HomeUiState(
                isLoading = true,
                selectedImageUri = selectedImageUri
            )
        }
    }

    fun getSelectedFaces(): List<FaceDetectedItem> {
        val faceList = uiState.value.faceList
        LogManager.d("HomeViewModel", "total faces: ${faceList.size}")
        return uiState.value.faceList
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
            activeSessionId = saveFacesUseCase(selectedFaces)
            LogManager.d(TAG, "activeSessionId: $activeSessionId")
        }
        _uiState.setState {
            copy(
                isLoading = false,
                message = "",
                navigateToSearch = true
            )
        }
    }

    fun consumeNavigateToSearch() {
        _uiState.setState {
            copy(
                navigateToSearch = false
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
                faceList = faces,
                isLoading = false,
                message = "${faces.size} faces found"
            )
        }
    }

    private fun clearSelection() {
        _selectedFaceIds.update {
            emptySet()
        }
    }

    fun toggleFaceSelection(faceId: String) {
        if (_selectedFaceIds.value.size >= MAX_SELECTED_FACES) {
            _uiState.setState {
                copy(
                    errorMessage = "You can select maximum $MAX_SELECTED_FACES faces",
                )
            }
            return
        }
        _selectedFaceIds.update { currentSet ->
            if (currentSet.contains(faceId)) {
                currentSet - faceId
            } else {
                currentSet + faceId
            }
        }
        if (_selectedFaceIds.value.isEmpty()) {
            _uiState.setState {
                copy(message = "")
            }
        }
    }

    companion object {
        private const val MAX_SELECTED_FACES = 3
        private const val TAG = "HomeViewModel"
    }
}


sealed class HomeIntent {
    data class Search(val selectedImage: Uri, val selectedOption: TimeRange) : HomeIntent()
}