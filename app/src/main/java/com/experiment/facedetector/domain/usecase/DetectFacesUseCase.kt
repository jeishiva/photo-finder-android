package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.data.local.entities.FaceDetectionResult
import com.experiment.facedetector.domain.entities.LocalImageItem
import com.experiment.facedetector.domain.repo.FaceDetectionRepo

class FaceDetectionUseCase(
    private val faceDetectionRepo: FaceDetectionRepo
) {
    suspend operator fun invoke(localImageItem: LocalImageItem): FaceDetectionResult {
        return faceDetectionRepo.detectFaces(localImageItem)
    }
}
