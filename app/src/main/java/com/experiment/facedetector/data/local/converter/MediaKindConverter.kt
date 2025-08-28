package com.experiment.facedetector.data.local.converter

import androidx.room.TypeConverter
import com.experiment.facedetector.domain.entities.MediaKind

class MediaKindConverter {
    @TypeConverter
    fun fromMediaKind(kind: MediaKind): String {
        return kind.name
    }

    @TypeConverter
    fun toMediaKind(value: String): MediaKind {
        return MediaKind.valueOf(value)
    }
}
