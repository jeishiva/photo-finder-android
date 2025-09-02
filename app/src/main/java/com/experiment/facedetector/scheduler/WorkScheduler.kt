package com.experiment.facedetector.scheduler

import android.content.Context
import androidx.work.*
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.data.worker.MediaScanWorker
import java.util.concurrent.TimeUnit

class WorkScheduler {

    /**
     * Schedule periodic media scan every 12 hours.
     */
    fun schedulePeriodicMediaScan(context: Context) {
        val request = PeriodicWorkRequestBuilder<MediaScanWorker>(12, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MEDIA_SCAN_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        LogManager.d("TAG", "media scanner scheduled")
    }


    /**
     * Cancel all scheduled media scan work.
     */
    fun cancelMediaScan(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(MEDIA_SCAN_WORK)
    }

    companion object {
        private const val MEDIA_SCAN_WORK = "MediaScanWork"
        private const val TAG = "WorkScheduler"

    }
}
