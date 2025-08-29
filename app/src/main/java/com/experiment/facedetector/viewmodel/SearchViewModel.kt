package com.experiment.facedetector.viewmodel

import ExtractEmbeddingsUseCase
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.common.safeCancel
import com.experiment.facedetector.data.local.scanner.CameraMediaScanner
import com.experiment.facedetector.domain.entities.Media
import com.experiment.facedetector.domain.filter.MediaFilter
import com.experiment.facedetector.domain.usecase.facesearch.SearchSimilarPhotoUseCase
import com.experiment.facedetector.presentation.entities.SearchUiState
import com.experiment.facedetector.presentation.common.UiStateHolder
import com.experiment.facedetector.presentation.entities.FaceSearchItemUi
import com.experiment.facedetector.presentation.entities.MediaItemUi
import com.experiment.facedetector.presentation.entities.toUi
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
    savedStateHandle: SavedStateHandle,
    val embeddingUseCase: ExtractEmbeddingsUseCase,
    val searchPhotosPagedUseCase: SearchSimilarPhotoUseCase,
    val mediaScanner: CameraMediaScanner,
) : ViewModel() {

    private val _uiState = UiStateHolder<SearchUiState>(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.state
    var searchJob: Job? = null
    private var searchSessionId: String = savedStateHandle.get<String>("sessionId")!!
    private val _filter = MutableStateFlow(MediaFilter())
    val filter: StateFlow<MediaFilter> = _filter.asStateFlow()
    private val searchTrigger: MutableStateFlow<List<FloatArray>> = MutableStateFlow(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedFaces: StateFlow<PagingData<MediaItemUi>> =
        // new embeddings OR DB change → consider re-running,
        // but only proceed when embeddings are non-empty.
        searchTrigger
        .filter { embeddings ->
            embeddings.isNotEmpty()
        }
        .flatMapLatest { embeddings ->
            searchPhotosPagedUseCase(embeddings)
        }
        .map { pagingData ->
            pagingData.map {
                it.toUi()
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PagingData.empty()
        )

    fun searchFaces(searchItems: List<FaceSearchItemUi>) {
        LogManager.d(TAG, "Search faces: ${searchItems.size}")
        searchJob?.safeCancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            startLoading()
            updateSearchItems(searchItems)
            val embeddings = searchItems.mapNotNull { searchItem ->
                embeddingUseCase(searchItem.faceBitmap).fold(
                    onSuccess = { embedding ->
                        LogManager.d(TAG, "Successfully extracted embedding for search item")
                        embedding
                    },
                    onFailure = { exception ->
                        LogManager.e(
                            TAG,
                            "Failed to extract embedding for search item",
                            throwable = exception
                        )
                        null
                    }
                )
            }
            searchTrigger.value = embeddings
            endLoading()
        }
    }

    init {
        startIndex()
    }

    fun startIndex() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaScanner.sync()
        }
    }

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Start -> {
                searchFaces(intent.searchFaces)
            }
            is SearchIntent.ImageSelected -> {
                handleImageSelectedIntent(intent.mediaItemUi)
            }
        }
    }

    private fun handleImageSelectedIntent(mediaItemUi: MediaItemUi) {
        LogManager.d(TAG, "handle image selected $mediaItemUi")
    }

    fun invalidSessionState() {
        _uiState.setState {
            copy(
                isLoading = false,
                errorMessage = "Session not found",
            )
        }
    }

    fun updateSearchItems(searchFaces: List<FaceSearchItemUi>) {
        LogManager.d(TAG, "updateSearchItems ${searchFaces.size}")
        _uiState.setState {
            copy(
                faceList = searchFaces,
            )
        }
    }

    fun startLoading() {
        _uiState.setState {
            copy(isLoading = true)
        }
    }

    fun endLoading() {
        _uiState.setState {
            copy(isLoading = false)
        }
    }

    sealed class SearchIntent {
        data class Start(val searchFaces: List<FaceSearchItemUi>) : SearchIntent()
        data class ImageSelected(val mediaItemUi: MediaItemUi) : SearchIntent()
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }
}
