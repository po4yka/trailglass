package com.po4yka.trailglass.di

import android.content.Context
import com.po4yka.trailglass.location.GeofencingClientWrapper
import com.po4yka.trailglass.location.RegionSyncObserver
import com.po4yka.trailglass.notification.GeofenceNotificationManager
import kotlinx.coroutines.CoroutineScope

/**
 * Android-specific module for geofencing and region monitoring components.
 *
 * These components are Android-specific and live in the composeApp module,
 * so they cannot be part of the shared module's DI graph.
 *
 * Usage:
 * ```kotlin
 * val geofencingModule = AndroidGeofencingModule(context, appComponent)
 * val geofencingClient = geofencingModule.geofencingClientWrapper
 * val notificationManager = geofencingModule.geofenceNotificationManager
 * val regionSyncObserver = geofencingModule.regionSyncObserver
 * ```
 */
class AndroidGeofencingModule(
    private val context: Context,
    private val appComponent: com.po4yka.trailglass.di.AppComponent
) {
    /**
     * Provides GeofencingClientWrapper for managing Android geofences.
     * Singleton instance per module.
     */
    val geofencingClientWrapper: GeofencingClientWrapper by lazy {
        GeofencingClientWrapper(context)
    }

    /**
     * Provides GeofenceNotificationManager for displaying geofence notifications.
     * Singleton instance per module.
     */
    val geofenceNotificationManager: GeofenceNotificationManager by lazy {
        GeofenceNotificationManager(context)
    }

    /**
     * Provides RegionSyncObserver for syncing regions with geofencing client.
     * Singleton instance per module.
     */
    val regionSyncObserver: RegionSyncObserver by lazy {
        RegionSyncObserver(
            context = context,
            regionRepository = appComponent.regionRepository,
            geofencingClientWrapper = geofencingClientWrapper,
            scope = appComponent.applicationScope,
            userId = appComponent.userId
        )
    }

    /**
     * Initialize geofencing monitoring.
     * Should be called when the app starts tracking location or when regions are enabled.
     */
    fun startGeofencingMonitoring() {
        regionSyncObserver.startObserving()
    }

    /**
     * Stop geofencing monitoring.
     * Should be called when location tracking stops or app is shutting down.
     */
    fun stopGeofencingMonitoring() {
        regionSyncObserver.stopObserving()
    }
}
