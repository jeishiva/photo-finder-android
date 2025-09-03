package com.experiment.facedetector.initializer

import android.app.Application
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.scheduler.WorkScheduler

class AppInitializer(private val application: Application) {

    fun init() {
        LogManager.d(TAG, "initializing app")
        initKoin()
        initWorkScheduler()
    }

    private fun initKoin() = KoinInitializer.init(application)

    private fun initWorkScheduler() {
        val workScheduler: WorkScheduler = org.koin.java.KoinJavaComponent.getKoin().get()
        workScheduler.schedulePeriodicMediaScan()
    }

    companion object {
        const val TAG = "AppInitializer"
    }
}
