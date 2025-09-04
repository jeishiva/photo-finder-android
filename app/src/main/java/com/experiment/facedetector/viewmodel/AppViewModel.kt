package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.common.extension.safeCancel
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.domain.usecase.ScanMediaUseCase
import com.experiment.facedetector.scheduler.WorkScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class AppViewModel(
    private val scanMediaUseCase: ScanMediaUseCase,
    private val workScheduler: WorkScheduler
) : ViewModel() {

    private var mediaScannerJob: Job? = null

    fun startMediaScanning() {
        LogManager.d(TAG, "Starting media scan")

        workScheduler.cancelOngoingMediaScan()
        mediaScannerJob?.safeCancel()
        mediaScannerJob = viewModelScope.launch {
            try {
                scanMediaUseCase()
            } catch (e: CancellationException) {
                LogManager.d(TAG, "Scan cancelled")
            } catch (e: Exception) {
                LogManager.e(TAG, "Scan failed", e)
            }
        }
    }

    companion object {
        const val TAG = "AppViewModel"
    }
}
