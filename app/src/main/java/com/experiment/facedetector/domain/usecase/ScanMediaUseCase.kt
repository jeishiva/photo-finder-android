package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.scanner.CameraMediaScanner
import com.experiment.facedetector.domain.entities.SyncConfig

class ScanMediaUseCase(
    private val mediaScanner: CameraMediaScanner,
) {
    suspend operator fun invoke() {
        LogManager.d(TAG, "media scanning started")
        val syncResult = mediaScanner.sync(SyncConfig())
        LogManager.d(TAG, "media scanning completed $syncResult")
    }

    companion object {
        const val TAG = "ScanMediaUseCase"
    }
}