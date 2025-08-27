package com.experiment.facedetector.data.local.converter

import androidx.room.TypeConverter
import com.experiment.facedetector.domain.entities.MediaSourceType

class MediaSourceTypeConverters {
    @TypeConverter
    fun toString(mediaSourceType: MediaSourceType?): String? {
        if (mediaSourceType == null) {
            return null
        }
        return mediaSourceType.key
    }

    @TypeConverter
    fun fromString(identifier: String?): MediaSourceType? {
        if (identifier == null) {
            return null
        }
        return MediaSourceTypes.fromIdentifier(identifier)
    }
}

object MediaSourceTypes {
    val all: List<MediaSourceType> = listOf(
        MediaSourceType.MediaStoreCamera,
        MediaSourceType.MediaStoreWhatsApp,
    )
    fun fromIdentifier(identifier: String): MediaSourceType =
        all.firstOrNull { it.key == identifier } ?: MediaSourceType.Unknown
}
