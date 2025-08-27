package com.experiment.facedetector.domain.source

import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.entities.SourceMediaItem

interface MediaSource {

    val sourceType: MediaSourceType

    suspend fun list(offset: Int, limit: Int): List<SourceMediaItem>
}