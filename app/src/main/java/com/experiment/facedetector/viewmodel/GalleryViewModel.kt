package com.experiment.facedetector.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.common.throttleFirst
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.LocalImageItem
import com.experiment.facedetector.domain.repo.DbInvalidationRepository
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.GetSyncedMediaUseCase
import com.experiment.facedetector.presentation.entities.GalleryUiState
import com.experiment.facedetector.presentation.common.UiStateHolder
import com.experiment.facedetector.presentation.entities.FaceSearchItemUi
import com.experiment.facedetector.presentation.entities.MediaItemUi
import com.experiment.facedetector.presentation.entities.toFaceSearchItem
import com.experiment.facedetector.presentation.entities.toUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class GalleryViewModel(
    val faceDetectionUseCase: FaceDetectionUseCase,
    val getSyncedMediaUseCase: GetSyncedMediaUseCase,
    val invalidationRepository: DbInvalidationRepository,
) : ViewModel() {

    private val _uiState = UiStateHolder<GalleryUiState>(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.state

    private val _selectedFaceIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFaceIds: StateFlow<Set<String>> = _selectedFaceIds

    private val refreshes: Flow<Unit> =
        invalidationRepository
            .changes("media")
            .onStart { emit(Unit) }


    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedSyncedMediaFlow: StateFlow<PagingData<MediaItemUi>> =
        refreshes
            .throttleFirst(500)
            .flatMapLatest {
                getSyncedMediaUseCase().map { pagingData ->
                    pagingData.map {
                        it.toUi()
                    }
                }
            }
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PagingData.empty()
            )

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Search -> {
                handleSearchIntent(intent)
            }

            is HomeIntent.ShowDetectedFaces -> {
                handleDetectedFacesIntent()
            }

            is HomeIntent.ImageSelected -> {
                handleImageSelectedIntent(intent.mediaItemUi)
            }
        }
    }

    private fun handleImageSelectedIntent(mediaItemUi: MediaItemUi) {
        LogManager.d(TAG, "handle image selected $mediaItemUi")
    }

    private fun handleDetectedFacesIntent() {
        LogManager.d(TAG, "handleDetectedFacesIntent ${uiState.value.faceList.size}")
        _uiState.setState {
            copy(showSelectedFaces = true)
        }
    }

    fun handleSearchIntent(intent: HomeIntent.Search) {
        intent.selectedImageUri ?: return
        setSelectedImage(intent.selectedImageUri)
        detectFaces(intent.selectedImageUri)
    }

    fun setSelectedImage(selectedImageUri: Uri?) {
        detectFaces(selectedImageUri!!)
        _uiState.setState {
            copy(selectedImageUri = selectedImageUri)
        }
    }

    fun detectFaces(selectedImage: Uri) {
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
            GalleryUiState(
                isLoading = true,
                selectedImageUri = selectedImageUri
            )
        }
    }

    fun getSearchItems(): List<FaceSearchItemUi> {
        val selectedIds = _selectedFaceIds.value
        val faceList = uiState.value.faceList
        LogManager.d(TAG, "Total faces: ${faceList.size}, Selected: ${selectedIds.size}")
        val result = faceList.filter { face ->
            selectedIds.contains(face.faceId)
        }.map {
            it.toFaceSearchItem()
        }
        LogManager.d(TAG, "Selected faces: ${result.size}")
        return result
    }

    fun triggerSearch() {
        _uiState.setState {
            copy(
                isLoading = false,
                message = "",
                navigateToSearch = true,
                sessionId = UUID.randomUUID().toString()
            )
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
                errorMessage = "No faces found",
                isLoading = false
            )
        }
    }

    fun sendDetectedFaces(faces: List<FaceDetectedItem>) {
        _uiState.setState {
            copy(
                faceList = faces,
                isLoading = false,
                message = "${faces.size} faces found",
                showSelectedFaces = true
            )
        }
    }

    fun markShowSelectedFacesHandled() {
        _uiState.setState {
            copy(showSelectedFaces = false)
        }
    }

    private fun clearSelection() {
        _selectedFaceIds.update {
            emptySet()
        }
    }

    fun toggleFaceSelection(faceId: String) {
        if (_selectedFaceIds.value.contains(faceId)
                .not() && _selectedFaceIds.value.size >= MAX_SELECTED_FACES
        ) {
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
        private const val MAX_SELECTED_FACES = 4
        private const val TAG = "HomeViewModel"
    }
}

sealed class HomeIntent {
    data class Search(val selectedImageUri: Uri?) : HomeIntent()
    data object ShowDetectedFaces : HomeIntent()
    data class ImageSelected(val mediaItemUi: MediaItemUi) : HomeIntent()
}