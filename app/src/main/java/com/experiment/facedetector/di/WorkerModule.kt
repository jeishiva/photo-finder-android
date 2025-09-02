package com.experiment.facedetector.di

import com.experiment.facedetector.data.worker.MediaScanWorker
import com.experiment.facedetector.scheduler.WorkScheduler
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workManagerModule = module {
    worker {
        MediaScanWorker(
            appContext = get(),
            params = get(),
            mediaScanner = get()
        )
    }

    single<WorkScheduler> {
        WorkScheduler()
    }
}