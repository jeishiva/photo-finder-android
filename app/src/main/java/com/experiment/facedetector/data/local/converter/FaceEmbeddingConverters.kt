package com.experiment.facedetector.data.local.converter

import androidx.room.TypeConverter
import com.experiment.facedetector.common.toByteArray
import com.experiment.facedetector.common.toFloatArray

class FaceEmbeddingConverters {
    @TypeConverter
    fun fromFloatArray(value: FloatArray): ByteArray = value.toByteArray()

    @TypeConverter
    fun toFloatArray(value: ByteArray): FloatArray = value.toFloatArray()
}
