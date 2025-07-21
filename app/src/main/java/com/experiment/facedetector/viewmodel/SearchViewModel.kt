package com.experiment.facedetector.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.experiment.facedetector.common.CAMERA_WORKER_TAG
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.domain.usecase.GetSearchQueryUseCase
import com.experiment.facedetector.domain.usecase.facesearch.AddFacesUseCase
import com.experiment.facedetector.domain.usecase.facesearch.GetAllEmbeddingsUseCase
import com.experiment.facedetector.domain.usecase.facesearch.SearchFaceUseCase
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    savedStateHandle: SavedStateHandle,
    val getSearchQueryUseCase: GetSearchQueryUseCase,
    val addFaceToGalleryUseCase: AddFacesUseCase,
    val getAllEmbeddingsUseCase: GetAllEmbeddingsUseCase,
    val searchFaceUseCase: SearchFaceUseCase,
    private val workManager: WorkManager
) : ViewModel() {
    private val _uiState = UiStateHolder<SearchUiState>(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.state
    private var searchSessionId: String? = savedStateHandle.get<String>("sessionId")

    init {
        startInitialWork()
        getSearchQuery(searchSessionId)
    }

    fun startInitialWork() {
        val workRequest = OneTimeWorkRequestBuilder<CameraImageWorker>()
            .addTag(CAMERA_WORKER_TAG)
            .build()
        workManager.enqueueUniqueWork(
            CAMERA_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getSearchQuery(sessionId: String?) {
        println(message = "getSearchQuery sessionId: $sessionId")
        if (sessionId == null) {
            _uiState.setState {
                copy(
                    errorMessage = "Session not found",
                    isLoading = false
                )
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.setState {
                copy(isLoading = true)
            }
            val faces = getSearchQueryUseCase(sessionId)
            println(message = "selected faces: ${faces.size}")
            addFaceToGalleryUseCase(faces)
            val embeddings = getAllEmbeddingsUseCase()
            println(message = "embeddings: ${embeddings.size}")
            _uiState.setState {
                copy(
                    faceList = faces,
                    isLoading = false
                )
            }
        }
    }
}