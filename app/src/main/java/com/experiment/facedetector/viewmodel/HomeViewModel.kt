package com.experiment.facedetector.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.experiment.facedetector.common.CAMERA_WORKER_TAG
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.worker.CameraImageWorker
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.map

class HomeViewModel(
    val faceDetectionUseCase: FaceDetectionUseCase,
    val saveFacesUseCase: SaveSearchQueryUseCase,
    val clearFacesUseCase: ClearSearchQueryUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    // home screen ui state
    private val _uiState = UiStateHolder<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.state

    // separate state flow for selected face ids - to be used in face selection
    // decided to use state flow here because it is easy to update and scalable for large dataset
    private val _selectedFaceIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFaceIds: StateFlow<Set<String>> = _selectedFaceIds

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
        selectedImageUri ?: return
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
        val selectedIds = _selectedFaceIds.value
        val faceList = uiState.value.faceList
        LogManager.d(TAG, "Total faces: ${faceList.size}, Selected: ${selectedIds.size}")
        return faceList.filter { face -> selectedIds.contains(face.faceId) }
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
                    navigateToSearch = true,
                    sessionId = sessionId
                )
            }
            LogManager.d(TAG, "activeSessionId: $sessionId")
        }
    }

    fun markNavigationHandled() {
        _uiState.setState {
            copy(
                navigateToSearch = false,
                sessionId = null
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
        if (_selectedFaceIds.value.contains(faceId).not() && _selectedFaceIds.value.size >= MAX_SELECTED_FACES) {
            _uiState.setState {
                copy(
                    message = "Maximum $MAX_SELECTED_FACES faces selected — oldest removed."
                )
            }
        }
        _selectedFaceIds.update { currentSet ->
            if (currentSet.contains(faceId)) {
                currentSet - faceId
            } else {
                if (currentSet.size >= MAX_SELECTED_FACES) {
                    val firstSelected = currentSet.first()
                    (currentSet - firstSelected) + faceId
                } else {
                    currentSet + faceId
                }
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