package com.experiment.facedetector.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.experiment.facedetector.common.CAMERA_WORKER_TAG
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.common.throttleFirst
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.domain.entities.FaceSearchItem
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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

    private val _workInfoStateFlow = MutableStateFlow<List<WorkInfo>>(emptyList())
    val isWorkerRunning: StateFlow<Boolean> = _workInfoStateFlow
        .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _workInfoStateFlow.value.any { it.state == WorkInfo.State.RUNNING }
        )

    private val searchTrigger = MutableStateFlow<List<FloatArray>>(emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedSearchFlow: Flow<PagingData<MediaWithFaces>> =
        searchTrigger
            .filter { it.isNotEmpty() }
            .flatMapLatest { embeddings ->
                searchFaceUseCase(embeddings)
            }
            .cachedIn(viewModelScope)
    private var searchSessionId: String? = savedStateHandle.get<String>("sessionId")
    private val _isAppendLoading = MutableStateFlow(false)
    private val _hasItems = MutableStateFlow(false)

    fun updatePagingState(isAppendLoading: Boolean, hasItems: Boolean) {
        _isAppendLoading.value = isAppendLoading
        _hasItems.value = hasItems
    }

    @OptIn(FlowPreview::class)
    val isAppending: StateFlow<Boolean> = combine(
        isWorkerRunning,
        _isAppendLoading,
        _hasItems
    ) { isWorker, isPagingAppend, hasItems ->
        LogManager.d("TAG", "isWorker: $isWorker, isPagingAppend: $isPagingAppend, hasItems: $hasItems")
        if (!hasItems) {
            isWorker
        } else {
            isPagingAppend || isWorker
        }
    }.throttleFirst(300)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    init {
        getSearchQuery(searchSessionId)
        observeWorkStatus()
        observeLoadingStatus()
    }
    
    @OptIn(FlowPreview::class)
    fun observeLoadingStatus() {
        viewModelScope.launch {
            isAppending.collect {
                println(message = "isAppending: $it")
               _uiState.setState {
                   copy(isLoading = it)
               } 
            }
        }
    }

    fun startInitialWork() {
        workManager.enqueueUniqueWork(
            CAMERA_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<CameraImageWorker>()
                .addTag(CAMERA_WORKER_TAG)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
    }

    @OptIn(FlowPreview::class)
    private fun observeWorkStatus() {
        workManager
            .getWorkInfosByTagLiveData(CAMERA_WORKER_TAG)
            .asFlow()
            .distinctUntilChanged()
            .onEach { it ->
                _workInfoStateFlow.value = it
            }
            .launchIn(viewModelScope)
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
            startInitialWork()
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
