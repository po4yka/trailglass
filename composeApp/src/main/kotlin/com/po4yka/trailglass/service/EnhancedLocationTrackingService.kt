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
import kotlin.math.roundToInt

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
        const val ACTION_PAUSE_TRACKING = "com.po4yka.trailglass.PAUSE_TRACKING"
        const val ACTION_RESUME_TRACKING = "com.po4yka.trailglass.RESUME_TRACKING"
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

        fun pauseService(context: Context) {
            val intent = Intent(context, EnhancedLocationTrackingService::class.java).apply {
                action = ACTION_PAUSE_TRACKING
            }
            context.startService(intent)
        }

        fun resumeService(context: Context) {
            val intent = Intent(context, EnhancedLocationTrackingService::class.java).apply {
                action = ACTION_RESUME_TRACKING
            }
            context.startService(intent)
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
    private var isPaused = false

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
            ACTION_PAUSE_TRACKING -> {
                pauseLocationTracking()
            }
            ACTION_RESUME_TRACKING -> {
                resumeLocationTracking()
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
            distance = 0.0,
            duration = 0,
            speed = null,
            isPaused = false
        )
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                locationTracker.startTracking(mode)
                isTracking = true
                isPaused = false

                // Observe tracking state and update notification
                locationTracker.trackingState
                    .onEach { state ->
                        if (state.isTracking) {
                            val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                            updateNotification(
                                distance = state.totalDistance,
                                duration = duration,
                                speed = state.currentSpeed,
                                locationCount = state.locationCount,
                                isPaused = false
                            )
                        }
                    }
                    .launchIn(serviceScope)

            } catch (e: Exception) {
                android.util.Log.e("EnhancedTrackingService", "Failed to start tracking", e)
                stopSelf()
            }
        }
    }

    private fun pauseLocationTracking() {
        if (!isTracking || isPaused) return

        serviceScope.launch {
            locationTracker.pauseTracking()
            isPaused = true

            // Update notification to show paused state
            val state = locationTracker.trackingState.value
            val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            updateNotification(
                distance = state.totalDistance,
                duration = duration,
                speed = null,
                isPaused = true
            )
        }
    }

    private fun resumeLocationTracking() {
        if (!isTracking || !isPaused) return

        serviceScope.launch {
            locationTracker.resumeTracking()
            isPaused = false

            // Update notification to show active state
            val state = locationTracker.trackingState.value
            val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            updateNotification(
                distance = state.totalDistance,
                duration = duration,
                speed = state.currentSpeed,
                isPaused = false
            )
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
        distance: Double,
        duration: Int,
        speed: Double?,
        locationCount: Int = 0,
        isPaused: Boolean
    ): Notification {
        // Format values
        val distanceText = formatDistance(distance)
        val durationText = formatDuration(duration)
        val speedText = speed?.let { formatSpeed(it) }

        // Create intents for actions
        val pauseResumeIntent = if (isPaused) {
            Intent(this, EnhancedLocationTrackingService::class.java).apply {
                action = ACTION_RESUME_TRACKING
            }
        } else {
            Intent(this, EnhancedLocationTrackingService::class.java).apply {
                action = ACTION_PAUSE_TRACKING
            }
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseResumeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

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

        // Build notification with custom layout
        val statusText = if (isPaused) "Paused" else "Tracking"
        val statusIcon = if (isPaused) R.drawable.ic_pause else R.drawable.ic_location

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TrailGlass - $statusText")
            .setContentText("$distanceText  •  $durationText" + (speedText?.let { "  •  $it" } ?: ""))
            .setSmallIcon(statusIcon)
            .setContentIntent(contentIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildString {
                        append("Distance: $distanceText\n")
                        append("Duration: $durationText\n")
                        if (speedText != null) {
                            append("Speed: $speedText\n")
                        }
                        append("Points: $locationCount")
                    })
            )
            .addAction(
                if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                if (isPaused) "Resume" else "Pause",
                pauseResumePendingIntent
            )
            .addAction(
                R.drawable.ic_stop,
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
        distance: Double,
        duration: Int,
        speed: Double?,
        locationCount: Int = 0,
        isPaused: Boolean
    ) {
        val notification = createNotification(distance, duration, speed, locationCount, isPaused)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.roundToInt()} m"
        } else {
            String.format("%.2f km", meters / 1000.0)
        }
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

    private fun formatSpeed(metersPerSecond: Double): String {
        val kmh = metersPerSecond * 3.6
        return String.format("%.1f km/h", kmh)
    }
}
