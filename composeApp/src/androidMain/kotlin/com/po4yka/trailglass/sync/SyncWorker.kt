package com.po4yka.trailglass.sync

import android.content.Context
import androidx.work.*
import com.po4yka.trailglass.TrailGlassApplication
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for background synchronization.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val logger = logger()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        logger.info { "SyncWorker started (attempt ${runAttemptCount + 1}/$MAX_RETRIES)" }

        try {
            // Get app component from Application class
            val application = applicationContext as? TrailGlassApplication
            if (application == null) {
                logger.error { "Cannot access TrailGlassApplication" }
                return@withContext Result.failure()
            }

            val syncManager = application.appComponent.syncManager

            // Perform full sync
            val result = syncManager.performFullSync()

            result.fold(
                onSuccess = { syncResult ->
                    logger.info {
                        "Background sync completed: " +
                        "${syncResult.uploaded} uploaded, " +
                        "${syncResult.downloaded} downloaded, " +
                        "${syncResult.conflicts} conflicts"
                    }
                    Result.success()
                },
                onFailure = { error ->
                    logger.error(error) { "Background sync failed" }
                    if (runAttemptCount < MAX_RETRIES) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            logger.error(e) { "Sync worker error" }

            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
        const val WORK_NAME = "trailglass_sync"
        const val TAG_SYNC = "sync"
        const val TAG_PERIODIC = "periodic"
    }
}

/**
 * Utility class for scheduling background sync.
 */
object SyncScheduler {

    private val logger = logger()

    /**
     * Schedule periodic background sync.
     */
    fun schedulePeriodicSync(
        context: Context,
        intervalMinutes: Long = 60
    ) {
        logger.info { "Scheduling periodic sync every $intervalMinutes minutes" }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(SyncWorker.TAG_SYNC)
            .addTag(SyncWorker.TAG_PERIODIC)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Trigger immediate one-time sync.
     */
    fun triggerImmediateSync(context: Context) {
        logger.info { "Triggering immediate sync" }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SyncWorker.TAG_SYNC)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    /**
     * Cancel all scheduled sync work.
     */
    fun cancelAllSync(context: Context) {
        logger.info { "Cancelling all scheduled sync work" }
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
    }

    /**
     * Check sync work status.
     */
    fun getSyncWorkInfo(context: Context) =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
}
