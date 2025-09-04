package com.experiment.facedetector.viewmodel

import com.experiment.facedetector.domain.usecase.ExtractEmbeddingsUseCase
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.common.extension.safeCancel
import com.experiment.facedetector.domain.filter.MediaFilter
import com.experiment.facedetector.domain.usecase.SearchSimilarPhotoUseCase
import com.experiment.facedetector.presentation.app.model.UiStateHolder
import com.experiment.facedetector.presentation.model.FaceSearchItemUi
import com.experiment.facedetector.presentation.model.MediaItemUi
import com.experiment.facedetector.presentation.model.toUi
import com.experiment.facedetector.presentation.screen.search.model.SearchIntent
import com.experiment.facedetector.presentation.screen.search.model.SearchUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val embeddingUseCase: ExtractEmbeddingsUseCase,
    private val searchPhotosPagedUseCase: SearchSimilarPhotoUseCase,
) : ViewModel() {

    private val _uiState = UiStateHolder<SearchUiState>(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.state

    private val _filter = MutableStateFlow(MediaFilter())
    val filter: StateFlow<MediaFilter> = _filter.asStateFlow()

    private val searchTrigger = MutableStateFlow<List<FloatArray>>(emptyList())
    private var searchJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedFaces: StateFlow<PagingData<MediaItemUi>> =
        searchTrigger
            .filter { embeddings -> embeddings.isNotEmpty() }
            .flatMapLatest { embeddings ->
                LogManager.d(TAG, "Triggering search with ${embeddings.size} embeddings")
                searchPhotosPagedUseCase(embeddings)
            }
            .map { pagingData ->
                pagingData.map { mediaItem -> mediaItem.toUi() }
            }
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PagingData.empty()
            )

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Start -> {
                LogManager.d(TAG, "Handle start search intent with ${intent.searchFaces.size} faces")
                performFaceSearch(intent.searchFaces)
            }
            is SearchIntent.ImageSelected -> {
                LogManager.d(TAG, "Handle image selected intent: ${intent.mediaItemUi.contentPath}")
                handleImageSelection(intent.mediaItemUi)
            }
            is SearchIntent.PhotoPreviewHandled -> {
                LogManager.d(TAG, "Handle photo preview handled intent")
                clearPhotoPreview()
            }
        }
    }

    private fun performFaceSearch(searchItems: List<FaceSearchItemUi>) {
        if (searchItems.isEmpty()) {
            LogManager.w(TAG, "Attempted to search with empty face list")
            return
        }

        LogManager.d(TAG, "Starting face search with ${searchItems.size} faces")

        // Cancel any existing search
        searchJob?.safeCancel()

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                setLoadingState(true)
                updateSearchFaceList(searchItems)

                val embeddings = extractEmbeddingsFromFaces(searchItems)

                if (embeddings.isNotEmpty()) {
                    LogManager.d(TAG, "Successfully extracted ${embeddings.size} embeddings, triggering search")
                    searchTrigger.value = embeddings
                } else {
                    LogManager.w(TAG, "No valid embeddings extracted from faces")
                }

            } catch (exception: Exception) {
                LogManager.e(TAG, "Face search failed", exception)
            } finally {
                setLoadingState(false)
            }
        }
    }

    private suspend fun extractEmbeddingsFromFaces(searchItems: List<FaceSearchItemUi>): List<FloatArray> {
        return searchItems.mapNotNull { searchItem ->
            embeddingUseCase(searchItem.faceBitmap).fold(
                onSuccess = { embedding ->
                    LogManager.d(TAG, "Successfully extracted embedding for face")
                    embedding
                },
                onFailure = { exception ->
                    LogManager.e(TAG, "Failed to extract embedding for face", exception)
                    null
                }
            )
        }
    }

    // MARK: - Image Selection Handling
    private fun handleImageSelection(mediaItemUi: MediaItemUi) {
        _uiState.setState {
            copy(previewPhotoPath = mediaItemUi.contentPath)
        }
    }

    private fun clearPhotoPreview() {
        _uiState.setState {
            copy(previewPhotoPath = null)
        }
    }

    // MARK: - UI State Updates
    private fun updateSearchFaceList(searchFaces: List<FaceSearchItemUi>) {
        LogManager.d(TAG, "Updating search face list with ${searchFaces.size} items")
        _uiState.setState {
            copy(faceList = searchFaces)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        LogManager.d(TAG, "Setting loading state: $isLoading")
        _uiState.setState {
            copy(isLoading = isLoading)
        }
    }

    override fun onCleared() {
        searchJob?.safeCancel()
        super.onCleared()
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }

}

