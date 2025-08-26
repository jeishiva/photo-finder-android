package com.experiment.facedetector.domain.repo

interface MediaFingerPrint {
    fun generate(
        sourceStableId: String,
        lastModified: Long?,
        sizeBytes: Long?
    ): String
}