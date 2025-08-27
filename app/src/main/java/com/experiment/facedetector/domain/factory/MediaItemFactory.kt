package com.experiment.facedetector.domain.factory

import com.experiment.facedetector.domain.entities.ImageSourceItem

object MediaItemFactory {
    fun createImage(
        stableId: Long,
        contentUri: String,
        mimeType: String,
        width: Int,
        height: Int,
        sizeBytes: Long,
        createdAtMs: Long,
        lastModifiedAtMs: Long,
        bucketId: Long? = null,
        generationModified: Long? = null,
        bucketDisplayName: String? = null,
        orientationDeg: Int? = null
    ): ImageSourceItem {
        return ImageSourceItem(
            stableId = stableId,
            contentUri = contentUri,
            mimeType = mimeType,
            width = width,
            height = height,
            sizeBytes = sizeBytes,
            createdAtMs = createdAtMs,
            lastModifiedAtMs = lastModifiedAtMs,
            bucketId = bucketId,
            generationModified = generationModified,
            bucketDisplayName = bucketDisplayName,
            orientationDeg = orientationDeg
        )
    }
}
