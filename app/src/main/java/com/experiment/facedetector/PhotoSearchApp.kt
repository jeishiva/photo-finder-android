package com.experiment.facedetector

import android.app.Application
import androidx.work.Configuration
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.initializer.KoinInitializer
import com.experiment.facedetector.initializer.WorkManagerInitializer

class PhotoSearchApp() : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        initializeApp()
    }

    fun initializeApp() {
        LogManager.d(TAG, "initializing app")
        KoinInitializer.init(this)
    }

    override val workManagerConfiguration: Configuration
        get() = WorkManagerInitializer.provideConfig()

    companion object {
        const val TAG = "PhotoSearchApp"
    }
}
