package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.data.local.entities.FaceDetectionResult
import com.experiment.facedetector.domain.entities.LocalImageItem

interface FaceDetectionRepo {
    suspend fun detectFaces(localImageItem: LocalImageItem): FaceDetectionResult
}