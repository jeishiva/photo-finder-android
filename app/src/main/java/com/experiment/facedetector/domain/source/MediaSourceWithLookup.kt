package com.experiment.facedetector.domain.source

interface MediaSourceWithLookup {
    suspend fun getByStableId(stableId: Long): SourceMediaItem?
}
