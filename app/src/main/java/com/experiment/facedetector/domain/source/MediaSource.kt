package com.experiment.facedetector.domain.source

import com.experiment.facedetector.domain.entities.MediaSourceType


interface MediaSource {
    val sourceType: MediaSourceType
}
