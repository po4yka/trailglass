package com.po4yka.trailglass.service

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker for periodic background location tracking.
 *
 * This worker runs periodically (default: every 15 minutes) to collect location samples
 * even when the app is in the background. It respects battery optimization settings and
 * can be configured for different tracking accuracies.
 *
 * For continuous tracking, use [LocationTrackingService] instead.
 */
class LocationTrackingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        const val WORK_NAME = "location_tracking_worker"
        private const val TRACKING_DURATION_MS = 60000L // Track for 1 minute per cycle

        /**
         * Schedules periodic background location tracking.
         *
         * @param context Android context
         * @param intervalMinutes Interval between tracking sessions (minimum 15 minutes)
         * @param requiresCharging Whether to only track when device is charging
         * @param requiresWifi Whether to require WiFi connectivity
         */
        fun schedulePeriodicTracking(
            context: Context,
            intervalMinutes: Long = 30,
            requiresCharging: Boolean = false,
            requiresWifi: Boolean = false
        ) {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(if (requiresWifi) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .setRequiresCharging(requiresCharging)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<LocationTrackingWorker>(
                    repeatInterval = intervalMinutes,
                    repeatIntervalTimeUnit = TimeUnit.MINUTES
                ).setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    ).addTag(WORK_NAME)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        /**
         * Cancels periodic background tracking.
         */
        fun cancelPeriodicTracking(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result =
        try {
            // Get LocationTracker from DI container
            // Note: Workers cannot use constructor injection. Use one of these patterns:
            // 1. Service Locator: Access AppComponent.locationTracker via Application class
            // 2. WorkManager InputData: Pass dependencies through worker parameters
            // 3. Singleton Pattern: Store tracker in Application class singleton
            //
            // Example implementation:
            // val app = applicationContext as MyApplication
            // val locationTracker = app.appComponent.locationTracker
            // locationTracker.startTracking(TrackingMode.SMART)
            // delay(TRACKING_DURATION_MS)
            // locationTracker.stopTracking()

            // For now, we'll track for a short duration
            // Implement when DI is set up
            delay(TRACKING_DURATION_MS)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
}
