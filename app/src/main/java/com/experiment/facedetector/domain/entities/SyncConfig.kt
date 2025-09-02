package com.experiment.facedetector.domain.entities

data class SyncConfig(
    val pageSize: Int = 48,
    val chunkSize: Int = 12,
    val maxConcurrency: Int = 2,
    val maxItemsPerRun: Int = 2000,
)