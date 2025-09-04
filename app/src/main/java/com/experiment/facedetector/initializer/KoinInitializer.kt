package com.experiment.facedetector.initializer

import android.app.Application
import com.experiment.facedetector.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.androidx.workmanager.koin.workManagerFactory

object KoinInitializer {
    fun init(app: Application) {
        startKoin {
            androidContext(app)
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
                    mediaSourceModule,
                    scannerModule,
                    matcherModule,
                    faceRecognitionModule,
                    workManagerModule
                )
            )
        }
    }
}
