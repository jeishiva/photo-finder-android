package com.experiment.facedetector.scheduler

import android.content.Context
import androidx.work.*
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.data.worker.MediaScanWorker
import com.experiment.facedetector.viewmodel.AppViewModel
import java.util.concurrent.TimeUnit

class WorkScheduler(
    private val context: Context,
    private val workManager: WorkManager
) {

    /**
     * Schedule periodic media scan every 12 hours.
     */
    fun schedulePeriodicMediaScan() {
        val request = PeriodicWorkRequestBuilder<MediaScanWorker>(12, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(false)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            MEDIA_SCAN_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        LogManager.d("TAG", "media scanner scheduled")
    }

    /**
     * Cancels only currently running media scan work (future schedules stay alive).
     */
    fun cancelOngoingMediaScan() {
        val operation = WorkManager.getInstance(context).cancelAllWorkByTag("MEDIA_SCAN_WORK")
        LogManager.d(TAG, "cancelled ongoing media scan, $operation")
    }

    companion object {
        private const val MEDIA_SCAN_WORK = "MediaScanWork"
        private const val TAG = "WorkScheduler"
    }
}
