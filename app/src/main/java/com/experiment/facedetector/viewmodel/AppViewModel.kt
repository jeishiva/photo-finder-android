package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.common.safeCancel
import com.experiment.facedetector.domain.usecase.ScanMediaUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AppViewModel(val scanMediaUseCase: ScanMediaUseCase) : ViewModel() {
    var indexingJob : Job? = null

    fun startMediaScanning() {
        LogManager.d(TAG, "media scanning started")
        indexingJob?.safeCancel()
        indexingJob = viewModelScope.launch {
            scanMediaUseCase()
        }
    }

    companion object {
        const val TAG = "AppViewModel"
    }
}
