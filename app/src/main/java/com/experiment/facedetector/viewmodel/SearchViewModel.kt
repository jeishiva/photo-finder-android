package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.common.UiStateHolder
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel() : ViewModel() {
    private val _uiState = UiStateHolder<SearchUiState>(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.state

   init {
       _uiState.setState {
           SearchUiState(
               isLoading = false,
               faceList = emptyList(),
           )
       }
   }
}