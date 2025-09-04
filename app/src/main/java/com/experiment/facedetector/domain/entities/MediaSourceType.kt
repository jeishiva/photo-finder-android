package com.experiment.facedetector.domain.entities

/**
 * A logical container under a media source.
 * @param relative Relative path/key within the source.
 * @param name  Human-friendly label to show in UI.
 */
data class Bucket(
    val relative: String,
    val name: String,
)

sealed class MediaSourceType(
    open val key: String,
    open val buckets: List<Bucket>,
) {
    // Abstract property to control registration
    abstract val shouldRegister: Boolean

    // register the media source type
    init {
        if (shouldRegister) {
            MediaSourceTypes.register(this)
        }
    }

    // ---- Local sources ----
    data class Local(
        override val key: String = LOCAL_DEVICE,
        override val buckets: List<Bucket> = emptyList(),
        override val shouldRegister: Boolean = true,
    ) : MediaSourceType(key, buckets)

    // ---- Remote sources ----
    data class Cloud(
        val provider: String,
        override val buckets: List<Bucket> = emptyList(),
        override val shouldRegister: Boolean = true,
    ) : MediaSourceType(key = buildString {
        append(CLOUD_PREFIX)
        append("_")
        append(provider.lowercase().replace(' ', '_'))
    },  buckets) {
        companion object {
            // sample remote sources
            val GooglePhotos = Cloud("google_drive")
            val Dropbox = Cloud("dropbox")
        }
    }

    // ---- Unknown sources ----
    object Unknown : MediaSourceType(UNKNOWN, emptyList()) {
        override val shouldRegister = false
    }

    companion object {
        const val LOCAL_DEVICE = "local_device"
        const val UNKNOWN = "unknown"
        const val CLOUD_PREFIX = "remote_"

        // media sources
        val Local = Local(buckets = listOf(
            Bucket(relative = "DCIM/Camera", name = "Camera")
        ))
    }
}

object MediaSourceTypes {
    private val registry = mutableListOf<MediaSourceType>()
    val all: List<MediaSourceType> get() = registry + MediaSourceType.Unknown

    fun fromIdentifier(key: String): MediaSourceType =
        all.firstOrNull { it.key == key } ?: MediaSourceType.Unknown

    fun register(source: MediaSourceType) {
        if (source !is MediaSourceType.Unknown) {
            registry.add(source)
        }
    }
}

