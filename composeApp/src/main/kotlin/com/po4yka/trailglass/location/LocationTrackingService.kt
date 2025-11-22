package com.po4yka.trailglass.location

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
import com.po4yka.trailglass.location.tracking.TrackingMode
import kotlinx.coroutines.*

/**
 * Foreground service for continuous location tracking.
 * Displays a persistent notification to keep the service alive.
 */
class LocationTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var locationTracker: LocationTracker? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking(intent)
            ACTION_STOP_TRACKING -> stopTracking()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        serviceScope.cancel()
    }

    private fun startTracking(intent: Intent) {
        val mode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(EXTRA_TRACKING_MODE, TrackingMode::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(EXTRA_TRACKING_MODE) as? TrackingMode
            } ?: TrackingMode.ACTIVE

        // Start foreground service with notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Initialize locationTracker from dependency injection
        // Services cannot use constructor injection. Use one of these patterns:
        // 1. Service Locator: Access AppComponent.locationTracker via Application class
        // 2. Manual Factory: Create tracker manually with required dependencies
        // 3. Assisted Injection: Use @AssistedInject with AssistedFactory
        //
        // Example implementation:
        // val app = application as MyApplication
        // locationTracker = app.appComponent.locationTracker
        //
        // Then start tracking:
        serviceScope.launch {
            try {
                locationTracker?.startTracking(mode)
            } catch (e: Exception) {
                android.util.Log.e("LocationTrackingService", "Failed to start tracking", e)
                stopSelf()
            }
        }
    }

    private fun stopTracking() {
        serviceScope.launch {
            locationTracker?.stopTracking()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Persistent notification for location tracking"
                    setShowBadge(false)
                }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Intent to open the app when notification is tapped
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        // Stop action
        val stopIntent =
            Intent(this, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
        val stopPendingIntent =
            PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("TrailGlass")
            .setContentText("Recording your location")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // App icon for notification
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            ).setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "location_tracking"
        private const val NOTIFICATION_ID = 1001

        private const val ACTION_START_TRACKING = "com.po4yka.trailglass.START_TRACKING"
        private const val ACTION_STOP_TRACKING = "com.po4yka.trailglass.STOP_TRACKING"
        private const val EXTRA_TRACKING_MODE = "tracking_mode"

        /**
         * Start the location tracking service.
         */
        fun start(
            context: Context,
            mode: TrackingMode = TrackingMode.ACTIVE
        ) {
            val intent =
                Intent(context, LocationTrackingService::class.java).apply {
                    action = ACTION_START_TRACKING
                    putExtra(EXTRA_TRACKING_MODE, mode)
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stop the location tracking service.
         */
        fun stop(context: Context) {
            val intent =
                Intent(context, LocationTrackingService::class.java).apply {
                    action = ACTION_STOP_TRACKING
                }
            context.startService(intent)
        }
    }
}
