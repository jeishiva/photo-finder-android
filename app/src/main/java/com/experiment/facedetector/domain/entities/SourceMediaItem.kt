package com.experiment.facedetector.domain.entities

sealed class SourceMediaItem {
    abstract val stableId: Long
    abstract val contentUri: String
    abstract val mimeType: String
    abstract val width: Int
    abstract val height: Int
    abstract val sizeBytes: Long
    abstract val createdAtMs: Long
    abstract val lastModifiedAtMs: Long
    abstract val bucketId: Long?
    abstract val generationModified: Long?
    abstract val bucketDisplayName: String?
}