package com.experiment.facedetector

import android.app.Application
import androidx.work.Configuration
import com.experiment.facedetector.initializer.AppInitializer
import com.experiment.facedetector.initializer.WorkManagerInitializer

class PhotoSearchApp() : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        AppInitializer(this).init()
    }

    override val workManagerConfiguration: Configuration
        get() = WorkManagerInitializer.provideConfig()

    companion object {
        const val TAG = "PhotoSearchApp"
    }
}
