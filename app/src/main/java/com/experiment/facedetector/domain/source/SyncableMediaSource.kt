package com.experiment.facedetector.domain.source

import com.experiment.facedetector.domain.entities.SyncConfig
import com.experiment.facedetector.domain.entities.SyncResult

interface SyncableMediaSource {
    suspend fun sync(config: SyncConfig = SyncConfig()): SyncResult
}