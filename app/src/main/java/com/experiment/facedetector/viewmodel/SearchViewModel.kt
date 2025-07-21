package com.experiment.facedetector.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.experiment.facedetector.common.CAMERA_WORKER_TAG
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.usecase.GetSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.facesearch.ExtractEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchPhotosPagedUseCase
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
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
        println(message = "getSearchQuery sessionId: $sessionId")
        if (sessionId == null) {
            _uiState.setState {
                copy(
                    errorMessage = "Session not found", isLoading = false
                )
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.setState {
                copy(isLoading = true)
            }
            val searchFaces = getSearchQueryUseCase(sessionId)
            val embeddings = searchFaces.mapNotNull {
                embeddingUseCase(it.thumbnailPath)
            }
            searchFaceUseCase.invoke(embeddings)
                .cachedIn(viewModelScope)
                .debounce(200)
                .onEach {
                    LogManager.d("SearchViewModel", "similar faces emitted")
                }.launchIn(viewModelScope)
            _uiState.setState {
                copy(
                    faceList = searchFaces, isLoading = false
                )
            }
        }
    }
}