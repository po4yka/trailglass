package com.po4yka.trailglass.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.po4yka.trailglass.R
import com.po4yka.trailglass.TrailGlassApplication
import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.location.tracking.TrackingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Enhanced Foreground Service for location tracking with live notification updates.
 *
 * Displays real-time tracking metrics (distance, duration, speed) in a notification,
 * similar to iOS Live Activities.
 *
 * Features:
 * - Real-time distance, duration, and speed display
 * - Pause/Resume/Stop actions
 * - Auto-updates every few seconds
 * - Low battery impact with efficient updates
 */
class EnhancedLocationTrackingService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"

        const val ACTION_START_TRACKING = "com.po4yka.trailglass.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.po4yka.trailglass.STOP_TRACKING"
        const val EXTRA_TRACKING_MODE = "tracking_mode"

        fun startService(context: Context, mode: TrackingMode = TrackingMode.ACTIVE) {
            val intent = Intent(context, EnhancedLocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_TRACKING_MODE, mode.name)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, EnhancedLocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val locationTracker: LocationTracker by lazy {
        (application as TrailGlassApplication).appComponent.locationTracker
    }

    private var startTime: Long = 0L
    private var isTracking = false
    private var sampleCount = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val modeName = intent.getStringExtra(EXTRA_TRACKING_MODE) ?: TrackingMode.ACTIVE.name
                val mode = try {
                    TrackingMode.valueOf(modeName)
                } catch (e: IllegalArgumentException) {
                    TrackingMode.ACTIVE
                }
                startLocationTracking(mode)
            }
            ACTION_STOP_TRACKING -> {
                stopLocationTracking()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        serviceScope.cancel()
    }

    private fun startLocationTracking(mode: TrackingMode) {
        if (isTracking) return

        startTime = System.currentTimeMillis()
        val notification = createNotification(
            duration = 0,
            sampleCount = 0
        )
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                locationTracker.startTracking(mode)
                isTracking = true
                sampleCount = 0

                // Observe location updates and update notification
                locationTracker.locationUpdates
                    .onEach { _ ->
                        sampleCount++
                        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                        updateNotification(
                            duration = duration,
                            sampleCount = sampleCount
                        )
                    }
                    .launchIn(serviceScope)

            } catch (e: Exception) {
                android.util.Log.e("EnhancedTrackingService", "Failed to start tracking", e)
                stopSelf()
            }
        }
    }

    private fun stopLocationTracking() {
        if (!isTracking) return

        serviceScope.launch {
            locationTracker.stopTracking()
            isTracking = false
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time location tracking updates"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        duration: Int,
        sampleCount: Int
    ): Notification {
        // Format values
        val durationText = formatDuration(duration)

        // Create intent to stop tracking
        val stopIntent = Intent(this, EnhancedLocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Create intent to open app
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TrailGlass - Tracking")
            .setContentText("$durationText  •  $sampleCount points")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildString {
                        append("Duration: $durationText\n")
                        append("Location samples: $sampleCount")
                    })
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification(
        duration: Int,
        sampleCount: Int
    ) {
        val notification = createNotification(duration, sampleCount)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }
}
