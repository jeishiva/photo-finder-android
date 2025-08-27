package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.entities.SourceMediaItem
import com.experiment.facedetector.domain.source.SourceMediaItem

interface StableIdGenerator {
    fun generate(
        mediaSourceType: MediaSourceType,
        sourceMediaItem: SourceMediaItem
    ): Long
}