package com.experiment.facedetector.domain.entities

data class ImageSourceItem(
    override val stableId: Long,
    override val contentUri: String,
    override val mimeType: String,
    override val width: Int,
    override val height: Int,
    override val sizeBytes: Long,
    override val createdAtMs: Long,
    override val lastModifiedAtMs: Long,
    override val bucketId: Long? = null,
    override val generationModified: Long? = null,
    override val bucketDisplayName: String? = null,
    val orientationDeg: Int? = null,
) : SourceMediaItem() {
    init {
        require(stableId > 0) { "stableId must be positive" }
        require(contentUri.isNotBlank()) { "contentUri cannot be blank" }
        require(mimeType.isNotBlank()) { "mimeType cannot be blank" }
        require(width > 0 && height > 0) { "dimensions must be positive" }
        require(sizeBytes >= 0) { "sizeBytes cannot be negative" }
    }
}
