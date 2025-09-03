package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.common.extension.throttleFirst
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.LocalImageItem
import com.experiment.facedetector.domain.repo.DbInvalidationRepository
import com.experiment.facedetector.domain.usecase.FaceDetectionUseCase
import com.experiment.facedetector.domain.usecase.GetSyncedMediaUseCase
import com.experiment.facedetector.presentation.app.model.UiStateHolder
import com.experiment.facedetector.presentation.model.FaceSearchItemUi
import com.experiment.facedetector.presentation.model.MediaItemUi
import com.experiment.facedetector.presentation.model.toFaceSearchItem
import com.experiment.facedetector.presentation.model.toUi
import com.experiment.facedetector.presentation.screen.gallery.model.FaceExtractionState
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryIntent
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryNavigationEvent
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class GalleryViewModel(
    private val faceDetectionUseCase: FaceDetectionUseCase,
    private val getSyncedMediaUseCase: GetSyncedMediaUseCase,
    private val invalidationRepository: DbInvalidationRepository,
) : ViewModel() {

    private val _uiState = UiStateHolder<GalleryUiState>(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.state

    private val _selectedFaceIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFaceIds: StateFlow<Set<String>> = _selectedFaceIds

    private val _navigationEvents = MutableSharedFlow<GalleryNavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val refreshSignals: Flow<Unit> =
        invalidationRepository
            .changes("media")
            .throttleFirst(3000)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedSyncedMediaFlow: StateFlow<PagingData<MediaItemUi>> =
        getSyncedMediaUseCase().map { pagingData ->
            pagingData.map { it.toUi() }
        }
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PagingData.empty()
            )

    init {
        observeMediaChanges()
    }

    fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.ShowDetectedFaces -> handleShowDetectedFacesIntent()
            is GalleryIntent.ImageSelected -> handleImageSelectedIntent(intent.contentPath)
            is GalleryIntent.GalleryRefreshed -> handleRefreshedIntent()
            is GalleryIntent.ResetImageSelection -> handleResetFlow()
            is GalleryIntent.LaunchSearch -> handleLaunchSearchIntent()
        }
    }

    private fun handleLaunchSearchIntent() {
        viewModelScope.launch {
            val sessionId = UUID.randomUUID().toString()
            _navigationEvents.emit(GalleryNavigationEvent.ToSearch(sessionId))
        }
    }

    private fun handleShowDetectedFacesIntent() {
        LogManager.d(TAG, "Handle show detected faces intent")
        updateFaceExtractionState { it.copy(showSelectedFaces = true) }
    }

    private fun handleImageSelectedIntent(contentPath: String) {
        LogManager.d(TAG, "Handle image selected: $contentPath")
        detectFacesForImage(contentPath)
    }

    private fun handleRefreshedIntent() {
        LogManager.d(TAG, "Handle gallery refreshed")
        _uiState.setState { copy(showRefreshHint = false) }
    }

    fun detectFacesForImage(imagePath: String?) {
        if (imagePath.isNullOrBlank()) {
            LogManager.w(TAG, "Attempted to detect faces with null/empty image path")
            return
        }
        LogManager.d(TAG, "Starting face detection for: $imagePath")
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // reset state and start detection
                resetFaceSelectionState()
                setFaceDetectionInProgress(imagePath)

                // perform face detection
                val detectionResult = faceDetectionUseCase(
                    localImageItem = LocalImageItem(imagePath)
                )
                // handle results
                when {
                    detectionResult.faces.isEmpty() -> {
                        LogManager.d(TAG, "No faces found in image: $imagePath")
                        handleNoFacesDetected()
                    }

                    else -> {
                        LogManager.d(
                            TAG,
                            "Found ${detectionResult.faces.size} faces in: $imagePath"
                        )
                        handleFacesDetected(detectionResult.faces)
                    }
                }

            } catch (exception: Exception) {
                LogManager.e(TAG, "Face detection failed for: $imagePath", exception)
                handleFaceDetectionError(exception)
            }
        }
    }

    private fun setFaceDetectionInProgress(imagePath: String) {
        updateFaceExtractionState { currentState ->
            currentState.copy(
                isInProgress = true,
                selectedImagePath = imagePath,
                showSelectedFaces = false,
                faceList = emptyList(),
                showFaceSelectionSheet = true
            )
        }
        clearErrorMessages()
    }

    private fun handleNoFacesDetected() {
        updateFaceExtractionState { currentState ->
            currentState.copy(
                isInProgress = false,
                showSelectedFaces = false,
                faceList = emptyList()
            )
        }
        setErrorMessage("No faces found in the selected image")
    }

    private suspend fun handleFacesDetected(faces: List<FaceDetectedItem>) {
        updateFaceExtractionState { currentState ->
            currentState.copy(
                isInProgress = false,
                showSelectedFaces = true,
                faceList = faces
            )
        }
    }

    private suspend fun handleFaceDetectionError(exception: Exception) {
        updateFaceExtractionState { currentState ->
            currentState.copy(
                isInProgress = false,
                showSelectedFaces = false,
                faceList = emptyList(),
                showFaceSelectionSheet = false
            )
        }
        setErrorMessage("Failed to detect faces: ${exception.localizedMessage ?: "Unknown error"}")
    }

    fun toggleFaceSelection(faceId: String) {
        val currentSelection = _selectedFaceIds.value
        when {
            currentSelection.contains(faceId) -> {
                // deselect face
                _selectedFaceIds.update { it - faceId }
                clearMessageIfNoSelection()
            }
            currentSelection.size >= MAX_SELECTED_FACES -> {
                // replace oldest selection
                val oldestFaceId = currentSelection.first()
                _selectedFaceIds.update { (it - oldestFaceId) + faceId }
                setMessage("Maximum $MAX_SELECTED_FACES faces selected — oldest removed.")
            }
            else -> {
                // add new selection
                _selectedFaceIds.update { it + faceId }
                clearMessage()
            }
        }
        LogManager.d(TAG, "face selection updated. selected: ${_selectedFaceIds.value.size}")
    }

    private fun clearMessageIfNoSelection() {
        if (_selectedFaceIds.value.isEmpty()) {
            clearMessage()
        }
    }

    private fun resetFaceSelectionState() {
        _selectedFaceIds.update { emptySet() }
    }

    fun getSelectedFaceSearchItems(): List<FaceSearchItemUi> {
        val selectedIds = _selectedFaceIds.value
        val faceList = uiState.value.faceExtractionState.faceList
        LogManager.d(
            TAG,
            "getting search items - total faces: ${faceList.size}, selected: ${selectedIds.size}"
        )
        return faceList
            .filter { face -> selectedIds.contains(face.faceId) }
            .map { it.toFaceSearchItem() }
            .also { result ->
                LogManager.d(TAG, "generated ${result.size} search items")
            }
    }

    fun handleResetFlow() {
        LogManager.d(TAG, "show selected faces handled")
        updateFaceExtractionState { currentState ->
            currentState.copy(
                isInProgress = false,
                selectedImagePath = null,
                showSelectedFaces = false,
                showFaceSelectionSheet = false
            )
        }
    }

    private fun updateFaceExtractionState(
        update: (FaceExtractionState) -> FaceExtractionState,
    ) {
        _uiState.setState {
            copy(faceExtractionState = update(faceExtractionState))
        }
    }

    private fun setMessage(message: String) {
        _uiState.setState { copy(message = message) }
    }

    private fun clearMessage() {
        _uiState.setState { copy(message = null) }
    }

    private fun setErrorMessage(errorMessage: String) {
        _uiState.setState { copy(errorMessage = errorMessage, isLoading = false) }
    }

    private fun clearErrorMessages() {
        _uiState.setState { copy(errorMessage = null, message = null) }
    }

    private fun observeMediaChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshSignals.collect {
                LogManager.d(TAG, "Media changes detected - showing refresh button")
                _uiState.setState { copy(showRefreshHint = true) }
            }
        }
    }

    companion object {
        private const val MAX_SELECTED_FACES = 4
        private const val TAG = "GalleryViewModel"
    }
}