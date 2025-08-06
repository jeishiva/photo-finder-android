package com.experiment.facedetector.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.experiment.facedetector.data.local.converter.FaceEmbeddingConverters
import com.experiment.facedetector.data.local.dao.FaceDao
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.FaceEmbeddingEntity
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity

@Database(
    entities = [
        MediaEntity::class,
        FaceEmbeddingEntity::class,
        FaceEntity::class,
    ],
    version = 2
)
@TypeConverters(FaceEmbeddingConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun faceDao(): FaceDao
}