package com.experiment.facedetector.di

import androidx.work.WorkManager
import com.experiment.facedetector.core.image.BitmapHelper
import org.koin.dsl.module

val appModule = module {
    single {
        WorkManager.getInstance(get())
    }
    single {
        BitmapHelper(get())
    }
}




