package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.domain.source.MediaSource
import com.experiment.facedetector.domain.source.MediaSourceType
import com.experiment.facedetector.domain.source.SourceMediaItem

interface StableIdGenerator {
    fun generate(
        mediaSourceType: MediaSourceType,
        sourceMediaItem: SourceMediaItem
    ): Long
}