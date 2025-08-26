package com.experiment.facedetector.data.local.entities

import com.experiment.facedetector.domain.entities.FaceDetectedItem

data class FaceDetectionResult(val faces: List<FaceDetectedItem>)

data class MediaIdFingerprintRow(val mediaId: Long, val fingerprint: String?)

data class StableIdToMediaIdRow(
    val sourceStableId: String,
    val mediaId: Long
)

data class StableIdFingerprintRow(
    val sourceStableId: String,
    val fingerprint: String?
)