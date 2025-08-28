package com.experiment.facedetector.domain.entities

data class SyncConfig(
    val pageSize: Int = 100,
    val maxItemsPerRun: Int = 2000,
    val chunkSize: Int = 25,
    val maxConcurrency: Int = 5
)