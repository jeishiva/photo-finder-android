package com.experiment.facedetector.domain.processing

/**
 * Create a thumbnail from a local file path.
 * Returns absolute path to saved thumbnail or null on failure.
 */
interface ThumbnailGenerator {
    suspend fun generateFromFile(filePath: String, mediaId: Long): String?
}
