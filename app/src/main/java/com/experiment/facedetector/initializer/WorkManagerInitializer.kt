package com.experiment.facedetector.initializer

import androidx.work.Configuration
import android.util.Log

object WorkManagerInitializer {
    fun provideConfig(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
    }
}
