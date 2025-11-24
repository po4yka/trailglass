package com.po4yka.trailglass.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.po4yka.trailglass.R
import com.po4yka.trailglass.location.GeofencingClientWrapper
import com.po4yka.trailglass.location.RegionSyncObserver
import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.location.tracking.TrackingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground Service for continuous location tracking and geofence monitoring.
 *
 * This service runs in the foreground with a persistent notification, allowing continuous location tracking even when
 * the app is in the background or the screen is off. It's suitable for active trip recording and region monitoring.
 *
 * The service respects Android's battery optimization and doze mode by using a foreground service with a notification.
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
        fun startService(
            context: Context,
            mode: TrackingMode = TrackingMode.ACTIVE
        ) {
            val intent =
                Intent(context, LocationTrackingService::class.java).apply {
                    action = ACTION_START_TRACKING
                    putExtra(EXTRA_TRACKING_MODE, mode.name)
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Stops the location tracking service. */
        fun stopService(context: Context) {
            val intent =
                Intent(context, LocationTrackingService::class.java).apply {
                    action = ACTION_STOP_TRACKING
                }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * LocationTracker instance obtained from DI.
     *
     * Integration: Services cannot use constructor injection. Use one of these patterns:
     * 1. Service Locator: Access AppComponent.locationTracker via Application class
     * 2. Manual Factory: Create tracker manually with required dependencies
     * 3. Assisted Injection: Use @AssistedInject with AssistedFactory
     *
     * For simplicity, services typically use pattern #2 (manual creation) or store the component in Application and
     * retrieve it here.
     */
    private val locationTracker: LocationTracker? by lazy {
        // Example: (application as MyApplication).appComponent.locationTracker
        // For now, returns null to avoid crash - implement when DI is set up
        null
    }

    /**
     * Geofencing components for region monitoring.
     */
    private lateinit var geofencingClientWrapper: GeofencingClientWrapper
    private var regionSyncObserver: RegionSyncObserver? = null

    private var isTracking = false
    private var lastLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeGeofencing()
    }

    /**
     * Initialize geofencing components.
     */
    private fun initializeGeofencing() {
        geofencingClientWrapper = GeofencingClientWrapper(this)

        // TODO: Initialize RegionSyncObserver with RegionRepository from DI
        // Example:
        // val application = application as TrailGlassApplication
        // val regionRepository = application.appComponent.regionRepository
        // val userId = getCurrentUserId()
        // regionSyncObserver = RegionSyncObserver(
        //     context = this,
        //     regionRepository = regionRepository,
        //     geofencingClientWrapper = geofencingClientWrapper,
        //     scope = serviceScope,
        //     userId = userId
        // )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val modeName = intent.getStringExtra(EXTRA_TRACKING_MODE) ?: TrackingMode.ACTIVE.name
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
                // Start tracking with LocationTracker from DI
                locationTracker?.startTracking(mode)
                isTracking = true

                // Start monitoring regions
                regionSyncObserver?.startObserving(lastLocation)

                // Update notification to show active tracking
                updateNotification("Tracking your location and regions...")
            } catch (e: Exception) {
                // Handle error - permission denied, location service unavailable, etc.
                android.util.Log.e("LocationTrackingService", "Failed to start tracking", e)
                stopSelf()
            }
        }
    }

    private fun stopLocationTracking() {
        if (!isTracking) return

        serviceScope.launch {
            // Stop tracking with LocationTracker from DI
            locationTracker?.stopTracking()

            // Stop monitoring regions
            regionSyncObserver?.stopObserving()

            isTracking = false
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Update location for region prioritization.
     * Call this when a significant location change occurs.
     */
    private fun onLocationChanged(location: Location) {
        val previousLocation = lastLocation
        lastLocation = location

        // Resync geofences if moved significantly
        if (previousLocation != null && regionSyncObserver != null) {
            val distance = previousLocation.distanceTo(location)
            if (distance >= RegionSyncObserver.RESYNC_DISTANCE_METERS) {
                serviceScope.launch {
                    regionSyncObserver?.syncWithCurrentLocation(location)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
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
        // Create intent to open the app when notification is tapped
        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val stopIntent =
            Intent(this, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
        val stopPendingIntent =
            PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("TrailGlass")
            .setContentText("Starting location tracking...")
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

    private fun updateNotification(text: String) {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
