package com.experiment.facedetector.domain.entities

sealed class SyncResult {
    data class Success(
        val sourceKey: String,
        val pagesScanned: Int,
        val itemsFetched: Int,
        val itemsUpserted: Int,
        val itemsChanged: Int,
        val itemsProcessed: Int,
        val facesSaved: Int,
        val cursorAdvanced: Boolean
    ) : SyncResult()

    data class Failure(
        val sourceKey: String,
        val reason: String,
        val throwable: Throwable? = null,
        val pagesScanned: Int = 0,
        val itemsFetched: Int = 0
    ) : SyncResult()
}

