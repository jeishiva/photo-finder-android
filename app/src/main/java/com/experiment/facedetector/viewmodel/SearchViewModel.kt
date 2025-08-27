package com.experiment.facedetector.viewmodel

import ExtractEmbeddingsUseCase
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.common.safeCancel
import com.experiment.facedetector.common.throttleFirst
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.di.MediaIndexerFactory
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.domain.filter.MediaFilter
import com.experiment.facedetector.domain.repo.DbInvalidationRepository
import com.experiment.facedetector.domain.source.MediaSourceType
import com.experiment.facedetector.domain.usecase.facesearch.SearchPhotosPagedUseCase
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onStart

class SearchViewModel(
    savedStateHandle: SavedStateHandle,
    val embeddingUseCase: ExtractEmbeddingsUseCase,
    val searchPhotosPagedUseCase: SearchPhotosPagedUseCase,
    val mediaIndexerFactory: MediaIndexerFactory,
    val invalidationRepo: DbInvalidationRepository,
) : ViewModel() {

    private val _uiState = UiStateHolder<SearchUiState>(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.state

    var searchJob: Job? = null

    private val searchTrigger = MutableStateFlow<List<FloatArray>>(emptyList())
    private var searchSessionId: String = savedStateHandle.get<String>("sessionId")!!

    private val _filter = MutableStateFlow(MediaFilter())
    val filter: StateFlow<MediaFilter> = _filter.asStateFlow()

    private val refreshes: Flow<Unit> =
        invalidationRepo
            .changes("media", "face")
            .onStart { emit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedFaces: StateFlow<PagingData<MediaWithFaces>> =
        refreshes.throttleFirst(500).flatMapLatest {
                searchPhotosPagedUseCase().flow
            }
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PagingData.empty()
            )

    fun searchFaces(searchItems: List<FaceSearchItem>) {
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
                        LogManager.e(TAG, "Failed to extract embedding for search item", throwable = exception)
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
            val indexer = mediaIndexerFactory.getIndexer(MediaSourceType.MediaStoreCamera)
            indexer.refreshAll()
        }
    }

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Start -> {
                searchFaces(intent.searchFaces)
            }
        }
    }

    fun invalidSessionState() {
        _uiState.setState {
            copy(
                isLoading = false,
                errorMessage = "Session not found",
            )
        }
    }

    fun updateSearchItems(searchFaces: List<FaceSearchItem>) {
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
        data class Start(val searchFaces: List<FaceSearchItem>) : SearchIntent()
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }
}
