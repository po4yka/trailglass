package com.po4yka.trailglass.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.po4yka.trailglass.TrailGlassApplication
import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.RegionEventType
import com.po4yka.trailglass.notification.GeofenceNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * BroadcastReceiver for handling geofence transition events.
 * Receives events when user enters or exits a geofenced region.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != GeofencingClientWrapper.ACTION_GEOFENCE_EVENT) {
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        if (triggeringGeofences == null || triggeringGeofences.isEmpty()) {
            Log.w(TAG, "No triggering geofences found")
            return
        }

        // Get the transition type
        val eventType =
            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> RegionEventType.ENTER
                Geofence.GEOFENCE_TRANSITION_EXIT -> RegionEventType.EXIT
                else -> {
                    Log.w(TAG, "Unknown geofence transition: $geofenceTransition")
                    return
                }
            }

        // Get location if available
        val location = geofencingEvent.triggeringLocation
        val latitude = location?.latitude
        val longitude = location?.longitude

        // Process each triggered geofence
        scope.launch {
            try {
                val application = context.applicationContext as? TrailGlassApplication
                if (application == null) {
                    Log.e(TAG, "Application is not TrailGlassApplication")
                    return@launch
                }

                // Get repositories from DI
                val regionRepository = getRegionRepository(application)
                val notificationManager = GeofenceNotificationManager(context)

                triggeringGeofences.forEach { geofence ->
                    val regionId = geofence.requestId
                    handleGeofenceTransition(
                        regionRepository,
                        notificationManager,
                        regionId,
                        eventType,
                        latitude,
                        longitude
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing geofence transition", e)
            }
        }
    }

    /**
     * Handle a single geofence transition event.
     */
    private suspend fun handleGeofenceTransition(
        regionRepository: RegionRepository,
        notificationManager: GeofenceNotificationManager,
        regionId: String,
        eventType: RegionEventType,
        latitude: Double?,
        longitude: Double?
    ) {
        try {
            // Get the region details
            val region = regionRepository.getRegionById(regionId)
            if (region == null) {
                Log.w(TAG, "Region not found: $regionId")
                return
            }

            // Update region statistics
            val timestamp = Clock.System.now()
            when (eventType) {
                RegionEventType.ENTER -> {
                    regionRepository.updateEnterStats(regionId, timestamp)
                    Log.i(TAG, "Entered region: ${region.name}")

                    // Show notification if enabled
                    if (region.notificationsEnabled) {
                        notificationManager.showRegionEnterNotification(region)
                    }
                }

                RegionEventType.EXIT -> {
                    regionRepository.updateExitStats(regionId, timestamp)
                    Log.i(TAG, "Exited region: ${region.name}")

                    // Show notification if enabled
                    if (region.notificationsEnabled) {
                        notificationManager.showRegionExitNotification(region)
                    }
                }
            }

            // Log the transition for analytics
            Log.d(
                TAG,
                "Region transition: ${region.name} - $eventType at ${latitude ?: "unknown"}, ${longitude ?: "unknown"}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error handling geofence transition for region $regionId", e)
        }
    }

    /**
     * Get RegionRepository from the application component.
     * This is a placeholder - implement based on your DI setup.
     */
    private fun getRegionRepository(application: TrailGlassApplication): RegionRepository {
        // TODO: Get from DI container
        // Example: application.appComponent.regionRepository
        throw NotImplementedError("RegionRepository DI integration needed")
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}
