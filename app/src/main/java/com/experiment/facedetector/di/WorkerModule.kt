package com.experiment.facedetector.di

import androidx.work.WorkManager
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
        WorkScheduler(
            context = get(),
            workManager = get()
        )
    }

    single<WorkManager> {
        WorkManager.getInstance(context = get())
    }
}