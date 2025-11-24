package com.po4yka.trailglass.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.po4yka.trailglass.TrailGlassApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** WorkManager worker for background synchronization. */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            Log.i(TAG, "SyncWorker started (attempt ${runAttemptCount + 1}/$MAX_RETRIES)")

            try {
                // Get app component from Application class
                val application = applicationContext as? TrailGlassApplication
                if (application == null) {
                    Log.e(TAG, "Cannot access TrailGlassApplication")
                    return@withContext Result.failure()
                }

                val syncManager = application.appComponent.syncManager

                // Perform full sync
                val result = syncManager.performFullSync()

                result.fold(
                    onSuccess = { syncResult ->
                        Log.i(
                            TAG,
                            "Background sync completed: " +
                                "${syncResult.uploaded} uploaded, " +
                                "${syncResult.downloaded} downloaded, " +
                                "${syncResult.conflicts} conflicts"
                        )
                        Result.success()
                    },
                    onFailure = { error ->
                        // Handle auth errors gracefully - these are expected when user isn't logged in
                        val isAuthError = error.message?.contains("No access token available") == true ||
                            error.message?.contains("Authentication failed") == true

                        if (isAuthError) {
                            Log.d(TAG, "Sync skipped: User not authenticated")
                            // Return success to avoid retry spam - this is not a real failure
                            Result.success()
                        } else {
                            Log.e(TAG, "Background sync failed", error)
                            if (runAttemptCount < MAX_RETRIES) {
                                Result.retry()
                            } else {
                                Result.failure()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Sync worker error", e)

                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }

    companion object {
        private const val TAG = "SyncWorker"
        private const val MAX_RETRIES = 3
        const val WORK_NAME = "trailglass_sync"
        const val TAG_SYNC = "sync"
        const val TAG_PERIODIC = "periodic"
    }
}

/** Utility class for scheduling background sync. */
object SyncScheduler {
    private const val TAG = "SyncScheduler"

    /** Schedule periodic background sync. */
    fun schedulePeriodicSync(
        context: Context,
        intervalMinutes: Long = 60
    ) {
        Log.i(TAG, "Scheduling periodic sync every $intervalMinutes minutes")

        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

        val syncRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = intervalMinutes,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).setConstraints(constraints)
                .addTag(SyncWorker.TAG_SYNC)
                .addTag(SyncWorker.TAG_PERIODIC)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /** Trigger immediate one-time sync. */
    fun triggerImmediateSync(context: Context) {
        Log.i(TAG, "Triggering immediate sync")

        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val syncRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .addTag(SyncWorker.TAG_SYNC)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    /** Cancel all scheduled sync work. */
    fun cancelAllSync(context: Context) {
        Log.i(TAG, "Cancelling all scheduled sync work")
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
    }

    /** Check sync work status. */
    fun getSyncWorkInfo(context: Context) = WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
}
