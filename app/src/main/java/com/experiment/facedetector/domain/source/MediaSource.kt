package com.experiment.facedetector.domain.source

interface MediaSource {

    val sourceType: MediaSourceType

    suspend fun list(offset: Int, limit: Int): List<SourceMediaItem>
}