package com.experiment.facedetector.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.domain.usecase.GetSearchQueryUseCase
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    savedStateHandle: SavedStateHandle,
    val getSearchQueryUseCase: GetSearchQueryUseCase
) : ViewModel() {
    private val _uiState = UiStateHolder<SearchUiState>(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.state
    private var searchSessionId: String? = savedStateHandle.get<String>("sessionId")

    init {
        getSearchQuery(searchSessionId)
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
            println(message = "faces size: ${faces.size} sessionId: $sessionId total faces: ${faces.size}")
            _uiState.setState {
                copy(
                    faceList = faces,
                    isLoading = false
                )
            }
        }
    }
}