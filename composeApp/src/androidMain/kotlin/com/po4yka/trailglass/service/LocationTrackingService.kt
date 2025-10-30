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
import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.domain.model.TrackingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground Service for continuous location tracking.
 *
 * This service runs in the foreground with a persistent notification, allowing
 * continuous location tracking even when the app is in the background or the
 * screen is off. It's suitable for active trip recording.
 *
 * The service respects Android's battery optimization and doze mode by using
 * a foreground service with a notification.
 */
class LocationTrackingService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"

        const val ACTION_START_TRACKING = "com.po4yka.trailglass.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.po4yka.trailglass.STOP_TRACKING"
        const val EXTRA_TRACKING_MODE = "tracking_mode"

        /**
         * Starts the location tracking service.
         *
         * @param context Android context
         * @param mode Tracking mode (CONTINUOUS or SMART)
         */
        fun startService(context: Context, mode: TrackingMode = TrackingMode.SMART) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_TRACKING_MODE, mode.name)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stops the location tracking service.
         */
        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // TODO: Inject LocationTracker from DI container
    // private lateinit var locationTracker: LocationTracker

    private var isTracking = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val modeName = intent.getStringExtra(EXTRA_TRACKING_MODE) ?: TrackingMode.SMART.name
                val mode = TrackingMode.valueOf(modeName)
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

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                // TODO: Start tracking with LocationTracker
                // locationTracker.startTracking(mode)
                isTracking = true

                // Update notification to show active tracking
                updateNotification("Tracking your location...")
            } catch (e: Exception) {
                // Handle error - permission denied, location service unavailable, etc.
                stopSelf()
            }
        }
    }

    private fun stopLocationTracking() {
        if (!isTracking) return

        serviceScope.launch {
            // TODO: Stop tracking with LocationTracker
            // locationTracker.stopTracking()
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
                description = "Shows when TrailGlass is tracking your location"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // TODO: Create proper intent for opening the app
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TrailGlass")
            .setContentText("Starting location tracking...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // TODO: Use app icon
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
