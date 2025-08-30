package com.experiment.facedetector.data.local.converter

import androidx.room.TypeConverter
import com.experiment.facedetector.data.local.entities.MediaErrorCode

class MediaErrorCodeConverter {
    @TypeConverter
    fun fromMediaErrorCode(mediaErrorCode: MediaErrorCode?): Int? {
        return mediaErrorCode?.code
    }

    @TypeConverter
    fun toMediaErrorCode(code: Int?): MediaErrorCode? {
        return code?.let { value ->
            MediaErrorCode.entries.firstOrNull { it.code == value }
        }
    }
}
