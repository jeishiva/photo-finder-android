package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.entities.SourceMediaItem

interface StableIdGenerator {
    fun generate(
        mediaSourceType: MediaSourceType,
        sourceMediaItem: SourceMediaItem
    ): Long
}