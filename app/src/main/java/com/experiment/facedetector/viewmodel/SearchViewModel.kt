package com.experiment.facedetector.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.experiment.facedetector.common.CAMERA_WORKER_TAG
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.domain.entities.ProcessedMediaItem
import com.experiment.facedetector.domain.usecase.GetSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.facesearch.ExtractEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchPhotosPagedUseCase
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchViewModel(
    savedStateHandle: SavedStateHandle,
    val getSearchQueryUseCase: GetSearchQueryUseCase,
    val searchFaceUseCase: SearchPhotosPagedUseCase,
    val embeddingUseCase: ExtractEmbeddingsUseCase,
    private val workManager: WorkManager,
) : ViewModel() {
    private val _uiState = UiStateHolder<SearchUiState>(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.state
    private var searchSessionId: String? = savedStateHandle.get<String>("sessionId")

    private val searchTrigger = MutableStateFlow<List<FloatArray>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedSearchFlow: Flow<PagingData<MediaWithFaces>> =
        searchTrigger.filter { it.isNotEmpty() }.flatMapLatest { embeddings ->
            searchFaceUseCase(embeddings)
        }.cachedIn(viewModelScope)

    init {
        startInitialWork()
        getSearchQuery(searchSessionId)
    }

    fun startInitialWork() {
        val workRequest =
            OneTimeWorkRequestBuilder<CameraImageWorker>().addTag(CAMERA_WORKER_TAG).build()
        workManager.enqueueUniqueWork(
            CAMERA_WORKER_TAG, ExistingWorkPolicy.REPLACE, workRequest
        )
    }

    @OptIn(FlowPreview::class)
    fun getSearchQuery(sessionId: String?) {
        println(message = "searchQuery sessionId: $sessionId")
        if (sessionId == null) {
            invalidSessionState()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            downloadEmbeddingCompleteState()
            val searchFaces = getSearchQueryUseCase(sessionId)
            searchQueryFoundState(searchFaces)
            for (face in searchFaces) {
                LogManager.d("SearchViewModel", "search face query: $face")
            }
            val embeddings = searchFaces.mapNotNull {
                embeddingUseCase(it.thumbnailPath)
            }
            searchTrigger.value = embeddings
            downloadingEmbeddingCompleteState()
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

    fun searchQueryFoundState(searchFaces: List<FaceSearchItem>) {
        _uiState.setState {
            copy(
                faceList = searchFaces,
            )
        }
    }

    fun downloadingEmbeddingCompleteState() {
        _uiState.setState {
            copy(
                isLoading = false
            )
        }
    }

    fun downloadEmbeddingCompleteState() {
        _uiState.setState {
            copy(isLoading = false)
        }
    }
}