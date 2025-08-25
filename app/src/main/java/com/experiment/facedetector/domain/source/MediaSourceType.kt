package com.experiment.facedetector.domain.source


/**
 * Identifiers for different types of media sources.
 * Replace hard-coded strings with these.
 */
sealed class MediaSourceType(val id: String) {
    // ---- Local sources ----
    object MediaStoreCamera : MediaSourceType("local_camera")

    // ---- Remote sources ----
    data class Cloud(val provider: String) : MediaSourceType("cloud_$provider")

    // Extend with other categories as needed
}
