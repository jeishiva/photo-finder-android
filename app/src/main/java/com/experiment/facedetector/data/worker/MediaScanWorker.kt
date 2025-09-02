package com.experiment.facedetector.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.experiment.facedetector.data.local.scanner.CameraMediaScanner
import com.experiment.facedetector.domain.entities.SyncResult
import com.experiment.facedetector.domain.usecase.ScanMediaUseCase

class MediaScanWorker(
    appContext: Context,
    params: WorkerParameters,
    private val cameraMediaScanner: CameraMediaScanner
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting media scan work")
        return try {
            val syncResult = ScanMediaUseCase(cameraMediaScanner)()
            handleSyncResult(syncResult)
        } catch (exception: Exception) {
            Log.e(TAG, "Unexpected error during media scan", exception)
            createFailureResult(
                sourceKey = "unknown",
                reason = "Unexpected error: ${exception.message}",
                errorMessage = exception.stackTraceToString()
            )
        }
    }

    private fun handleSyncResult(syncResult: SyncResult): Result {
        Log.d("MediaScanWorker", "Sync finished with result=$syncResult")
        return when (syncResult) {
            is SyncResult.Success -> {
                Log.d(TAG, "Media scan completed successfully: ${syncResult.itemsProcessed} items processed")
                createSuccessResult(syncResult)
            }

            is SyncResult.Failure -> {
                Log.w(TAG, "Media scan failed: ${syncResult.reason}", syncResult.throwable)
                handleFailureResult(syncResult)
            }
        }
    }

    private fun createSuccessResult(syncResult: SyncResult.Success): Result {
        return Result.success(
            workDataOf(
                KEY_SOURCE_KEY to syncResult.sourceKey,
                KEY_PAGES_SCANNED to syncResult.pagesScanned,
                KEY_ITEMS_FETCHED to syncResult.itemsFetched,
                KEY_ITEMS_UPSERTED to syncResult.itemsUpserted,
                KEY_ITEMS_CHANGED to syncResult.itemsChanged,
                KEY_ITEMS_PROCESSED to syncResult.itemsProcessed,
                KEY_FACES_SAVED to syncResult.facesSaved,
                KEY_CURSOR_ADVANCED to syncResult.cursorAdvanced
            )
        )
    }

    private fun handleFailureResult(syncResult: SyncResult.Failure): Result {
        return when {
            syncResult.throwable != null && isRetryableError(syncResult.throwable) -> {
                Log.i(TAG, "retryable error encountered, scheduling retry")
                Result.retry()
            }

            else -> {
                Log.e(TAG, "Non-retryable failure: ${syncResult.reason}")
                createFailureResult(
                    sourceKey = syncResult.sourceKey,
                    reason = syncResult.reason,
                    errorMessage = syncResult.throwable?.message,
                    pagesScanned = syncResult.pagesScanned,
                    itemsFetched = syncResult.itemsFetched
                )
            }
        }
    }

    private fun createFailureResult(
        sourceKey: String,
        reason: String,
        errorMessage: String? = null,
        pagesScanned: Int = 0,
        itemsFetched: Int = 0
    ): Result {
        val dataMap = mutableMapOf(
            KEY_SOURCE_KEY to sourceKey,
            KEY_FAILURE_REASON to reason,
            KEY_PAGES_SCANNED to pagesScanned,
            KEY_ITEMS_FETCHED to itemsFetched
        )
        errorMessage?.let { dataMap[KEY_ERROR_MESSAGE] = it }
        return Result.failure(workDataOf(*dataMap.toList().toTypedArray()))
    }

    private fun isRetryableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.net.SocketTimeoutException,
            is java.net.UnknownHostException,
            is java.io.IOException -> true

            // Add specific exceptions that should trigger retries
            else -> throwable.message?.contains("network", ignoreCase = true) == true ||
                    throwable.message?.contains("timeout", ignoreCase = true) == true
        }
    }
    companion object {
        private const val TAG = "MediaScanWorker"

        const val KEY_SOURCE_KEY = "sourceKey"
        const val KEY_PAGES_SCANNED = "pagesScanned"
        const val KEY_ITEMS_FETCHED = "itemsFetched"
        const val KEY_ITEMS_UPSERTED = "itemsUpserted"
        const val KEY_ITEMS_CHANGED = "itemsChanged"
        const val KEY_ITEMS_PROCESSED = "itemsProcessed"
        const val KEY_FACES_SAVED = "facesSaved"
        const val KEY_CURSOR_ADVANCED = "cursorAdvanced"
        const val KEY_FAILURE_REASON = "reason"
        const val KEY_ERROR_MESSAGE = "errorMessage"
    }
}