package com.experiment.facedetector.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.experiment.facedetector.data.local.converter.FloatArrayConverter
import com.experiment.facedetector.data.local.converter.MediaErrorCodeConverter
import com.experiment.facedetector.data.local.converter.MediaKindConverter
import com.experiment.facedetector.data.local.converter.MediaSourceTypeConverters
import com.experiment.facedetector.data.local.dao.FaceDao
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.dao.MediaSourceCursorDao
import com.experiment.facedetector.data.local.dao.MediaWithFacesDao
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.entities.SourceMediaCursor

@Database(
    entities = [
        MediaEntity::class,
        FaceEntity::class,
        SourceMediaCursor::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    FloatArrayConverter::class,
    MediaSourceTypeConverters::class,
    MediaKindConverter::class,
    MediaErrorCodeConverter::class,
    )
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun faceDao(): FaceDao
    abstract fun mediaWithFacesDao(): MediaWithFacesDao
    abstract fun mediaSourceCursorDao(): MediaSourceCursorDao
}
