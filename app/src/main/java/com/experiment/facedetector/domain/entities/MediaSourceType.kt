package com.experiment.facedetector.domain.entities


/**
 * Identifiers for different types of media sources.
 *
 */
sealed class MediaSourceType(val key: String) {

    // ---- Local sources ----
    object MediaStoreCamera : MediaSourceType(LOCAL_CAMERA)
    object MediaStoreWhatsApp : MediaSourceType(LOCAL_WHATSAPP)

    // ---- Remote sources ----
    data class Cloud(val provider: String) : MediaSourceType(provider.toCloudKey())

    // ---- Unknown sources ----
    object Unknown : MediaSourceType(UNKNOWN)

    companion object {
        const val LOCAL_CAMERA = "local_camera"
        const val LOCAL_WHATSAPP = "local_whatsapp"
        const val UNKNOWN = "unknown"
        const val CLOUD_PREFIX = "remote_"
    }
}

fun String.toCloudKey(): String {
    return buildString {
        append(MediaSourceType.CLOUD_PREFIX)
        append("_")
        append(this)
    }
}
