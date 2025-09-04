package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.data.local.scanner.LocalMediaScanner
import com.experiment.facedetector.domain.entities.SyncConfig
import com.experiment.facedetector.domain.entities.SyncResult

class ScanMediaUseCase(
    private val mediaScanner: LocalMediaScanner,
) {
    suspend operator fun invoke(): SyncResult {
        LogManager.d(TAG, "media scanning started")
        val syncResult = mediaScanner.sync(SyncConfig())
        LogManager.d(TAG, "media scanning completed $syncResult")
        return syncResult
    }

    companion object {
        const val TAG = "ScanMediaUseCase"
    }
}