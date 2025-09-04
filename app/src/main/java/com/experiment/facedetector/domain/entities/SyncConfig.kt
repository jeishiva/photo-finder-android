package com.experiment.facedetector.domain.entities

/**
 *  having max concurrency limit more than 2, can lead to aggressive killing by
 *  OEMs like one plus, huawei etc.
 *
 *  OEM is killing the app is due to sustained high CPU usage and memory usage
 */
data class SyncConfig(
    val pageSize: Int = 25,
    val chunkSize: Int = pageSize / 4,
    val maxConcurrency: Int = 2,
    val maxItemsPerRun: Int = 2000,
)