package com.experiment.facedetector.data.local.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.worker.processor.ICameraProcessor

class CameraImageWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val processor: ICameraProcessor,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        LogManager.d(message = "image worker started")
        return try {
            processor.process()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            LogManager.e(message = "CameraImageWorker failed: ${e.message}")
            Result.failure()
        }
    }
}
