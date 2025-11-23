package com.po4yka.trailglass.location

import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for periodic location data processing. Processes raw location samples into place visits, routes,
 * and trips.
 */
class LocationProcessingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                // Initialize dependencies from DI
                // Workers cannot use constructor injection. Use one of these patterns:
                // 1. Service Locator: Access AppComponent via Application class
                // 2. WorkManager InputData: Pass dependencies through worker parameters
                // 3. Singleton Pattern: Store dependencies in Application class
                //
                // Example implementation:
                // val app = applicationContext as MyApplication
                // val locationProcessor = app.appComponent.locationProcessor
                // val locationRepository = app.appComponent.locationRepository
                // val userId = app.sessionManager.getCurrentUserId()
                //
                // // Get unprocessed samples
                // val samples = locationRepository.getSamplesForUser(userId)
                //
                // // Process them
                // val result = locationProcessor.processLocationData(samples, userId)
                //
                // // Log results
                // android.util.Log.i(
                //     "LocationProcessingWorker",
                //     "Processed ${samples.size} samples: " +
                //     "${result.visits.size} visits, " +
                //     "${result.routes.size} routes, " +
                //     "${result.trips.size} trips"
                // )

                Result.success()
            } catch (e: Exception) {
                android.util.Log.e("LocationProcessingWorker", "Processing failed", e)

                // Retry on failure
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }

    companion object {
        private const val WORK_NAME = "location_processing"
        private const val TAG = "LocationProcessing"

        /**
         * Schedule periodic location processing.
         *
         * @param context Application context
         * @param intervalHours Interval between processing runs in hours (default: 6 hours)
         */
        fun schedule(
            context: Context,
            intervalHours: Long = 6
        ) {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // For geocoding
                    .setRequiresBatteryNotLow(true)
                    .build()

            val request =
                PeriodicWorkRequestBuilder<LocationProcessingWorker>(
                    intervalHours,
                    TimeUnit.HOURS,
                    1,
                    TimeUnit.HOURS // Flex window of 1 hour
                ).setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    ).addTag(TAG)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** Cancel scheduled processing. */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /** Trigger an immediate processing run. */
        fun triggerImmediate(context: Context) {
            val request =
                OneTimeWorkRequestBuilder<LocationProcessingWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag(TAG)
                    .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
