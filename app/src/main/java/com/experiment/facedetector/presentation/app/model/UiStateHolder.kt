package com.experiment.facedetector.presentation.app.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UiStateHolder<T>(initialState: T) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state.asStateFlow()

    fun setState(reducer: T.() -> T) {
        _state.update { it.reducer() }
    }
}