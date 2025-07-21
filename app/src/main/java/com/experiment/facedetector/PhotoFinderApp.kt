package com.experiment.facedetector

import android.app.Application
import com.experiment.facedetector.di.appModule
import com.experiment.facedetector.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import androidx.work.Configuration
import com.experiment.facedetector.di.faceDetectorModule
import com.experiment.facedetector.di.faceRecognitionModule
import com.experiment.facedetector.di.imageLoaderModule
import com.experiment.facedetector.di.networkModule
import com.experiment.facedetector.di.processorModule
import com.experiment.facedetector.di.repositoryModule
import com.experiment.facedetector.di.useCaseModule
import com.experiment.facedetector.di.viewModelModule
import org.koin.androidx.workmanager.koin.workManagerFactory

class PhotoFinderApp() : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        init()
    }

    fun init() {
        initKoin()
    }

    fun initKoin() {
        startKoin {
            androidContext(this@PhotoFinderApp)
            workManagerFactory()
            modules(
                listOf(
                    appModule,
                    networkModule,
                    faceDetectorModule,
                    imageLoaderModule,
                    databaseModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule,
                    processorModule,
                    faceRecognitionModule
                )
            )
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
