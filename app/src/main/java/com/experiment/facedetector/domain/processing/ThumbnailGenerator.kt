package com.experiment.facedetector.domain.processing

/**
 * Create a thumbnail from a local file path.
 * Returns absolute path to saved thumbnail or null on failure.
 */
interface ThumbnailGenerator {
    suspend fun extractFromFile(filePath: String, mediaId: Long): Result<String>
}
