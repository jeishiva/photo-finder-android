package com.experiment.facedetector.domain.index

import com.experiment.facedetector.domain.entities.SyncConfig
import com.experiment.facedetector.domain.entities.SyncResult

interface MediaScanner {
    suspend fun sync(config: SyncConfig = SyncConfig()): SyncResult
}