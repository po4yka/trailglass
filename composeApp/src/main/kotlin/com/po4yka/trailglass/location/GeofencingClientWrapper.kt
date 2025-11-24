package com.po4yka.trailglass.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.po4yka.trailglass.domain.model.Region
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.days

/**
 * Wrapper around Google Play Services GeofencingClient for managing geofences.
 * Provides a simplified API for adding, removing, and monitoring geofences based on Region objects.
 */
class GeofencingClientWrapper(
    private val context: Context
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    /**
     * Add geofences for a list of regions.
     * @param regions List of regions to monitor
     * @throws SecurityException if location permissions are not granted
     * @throws IllegalStateException if geofencing is not available
     */
    suspend fun addGeofences(regions: List<Region>) {
        if (regions.isEmpty()) return

        if (!hasLocationPermissions()) {
            throw SecurityException("Location permissions not granted")
        }

        // Convert regions to geofences
        val geofences =
            regions.mapNotNull { region ->
                if (region.notificationsEnabled) {
                    buildGeofence(region)
                } else {
                    null
                }
            }

        if (geofences.isEmpty()) return

        val request = buildGeofencingRequest(geofences)
        val pendingIntent = getGeofencePendingIntent()

        try {
            geofencingClient.addGeofences(request, pendingIntent).await()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to add geofences: ${e.message}", e)
        }
    }

    /**
     * Add a single geofence for a region.
     * @param region Region to monitor
     * @throws SecurityException if location permissions are not granted
     */
    suspend fun addGeofence(region: Region) {
        addGeofences(listOf(region))
    }

    /**
     * Remove geofences by region IDs.
     * @param regionIds List of region IDs to stop monitoring
     */
    suspend fun removeGeofences(regionIds: List<String>) {
        if (regionIds.isEmpty()) return

        try {
            geofencingClient.removeGeofences(regionIds).await()
        } catch (e: Exception) {
            // Log error but don't throw - geofence might already be removed
            android.util.Log.w(TAG, "Failed to remove geofences: ${e.message}")
        }
    }

    /**
     * Remove a single geofence.
     * @param regionId Region ID to stop monitoring
     */
    suspend fun removeGeofence(regionId: String) {
        removeGeofences(listOf(regionId))
    }

    /**
     * Remove all geofences.
     */
    suspend fun removeAllGeofences() {
        try {
            val pendingIntent = getGeofencePendingIntent()
            geofencingClient.removeGeofences(pendingIntent).await()
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Failed to remove all geofences: ${e.message}")
        }
    }

    /**
     * Build a Geofence from a Region.
     */
    private fun buildGeofence(region: Region): Geofence {
        var transitionTypes = 0
        if (region.notificationsEnabled) {
            // Monitor both ENTER and EXIT transitions
            transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        }

        return Geofence
            .Builder()
            .setRequestId(region.id)
            .setCircularRegion(
                region.latitude,
                region.longitude,
                region.radiusMeters.toFloat()
            ).setExpirationDuration(GEOFENCE_EXPIRATION_DURATION_MS)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(LOITERING_DELAY_MS)
            .build()
    }

    /**
     * Build a GeofencingRequest from a list of geofences.
     */
    private fun buildGeofencingRequest(geofences: List<Geofence>): GeofencingRequest =
        GeofencingRequest
            .Builder()
            .apply {
                // Trigger when device enters the geofence
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                addGeofences(geofences)
            }.build()

    /**
     * Get the PendingIntent for geofence transitions.
     * This intent will be broadcast to GeofenceBroadcastReceiver.
     */
    private fun getGeofencePendingIntent(): PendingIntent {
        val intent =
            Intent(context, GeofenceBroadcastReceiver::class.java).apply {
                action = ACTION_GEOFENCE_EVENT
            }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Check if the app has necessary location permissions.
     */
    private fun hasLocationPermissions(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val TAG = "GeofencingClientWrapper"

        // Action for geofence events
        const val ACTION_GEOFENCE_EVENT = "com.po4yka.trailglass.ACTION_GEOFENCE_EVENT"

        // Geofence expiration duration (30 days)
        private val GEOFENCE_EXPIRATION_DURATION_MS = 30.days.inWholeMilliseconds

        // Loitering delay (5 minutes) - time spent in geofence before DWELL trigger
        private const val LOITERING_DELAY_MS = 5 * 60 * 1000

        // Maximum number of geofences allowed by Android
        const val MAX_GEOFENCES = 100
    }
}
