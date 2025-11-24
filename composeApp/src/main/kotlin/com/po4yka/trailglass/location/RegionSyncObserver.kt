package com.po4yka.trailglass.location

import android.content.Context
import android.location.Location
import android.util.Log
import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.Region
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Observes changes to regions in the RegionRepository and syncs them with the GeofencingClient.
 * Handles the Android limit of 100 geofences by prioritizing nearby regions.
 * Also observes location changes to re-prioritize regions when user moves significantly.
 */
class RegionSyncObserver(
    private val context: Context,
    private val regionRepository: RegionRepository,
    private val geofencingClientWrapper: GeofencingClientWrapper,
    private val currentLocationProvider: CurrentLocationProvider,
    private val scope: CoroutineScope,
    private val userId: String
) {
    private var observerJob: Job? = null
    private var locationObserverJob: Job? = null
    private var currentRegions: Set<String> = emptySet()
    private var lastLocationUpdate: Location? = null

    /**
     * Start observing region changes and sync with GeofencingClient.
     * Also starts observing location changes to re-prioritize regions.
     * @param currentLocation Optional current location to prioritize nearby regions
     */
    fun startObserving(currentLocation: Location? = null) {
        observerJob?.cancel()
        locationObserverJob?.cancel()

        lastLocationUpdate = currentLocation

        // Observe region changes
        observerJob =
            scope.launch {
                regionRepository
                    .getAllRegions(userId)
                    .catch { e ->
                        Log.e(TAG, "Error observing regions", e)
                    }.collect { regions ->
                        syncGeofences(regions, lastLocationUpdate)
                    }
            }

        // Observe location changes to re-prioritize regions
        locationObserverJob =
            scope.launch {
                currentLocationProvider.observeLocation()
                    .catch { e ->
                        Log.e(TAG, "Error observing location", e)
                    }
                    .filter { locationData ->
                        // Only process significant location changes (5km+)
                        val lastLocation = lastLocationUpdate
                        if (lastLocation == null) {
                            true
                        } else {
                            val currentLoc = Location("").apply {
                                latitude = locationData.latitude
                                longitude = locationData.longitude
                            }
                            val distance = lastLocation.distanceTo(currentLoc)
                            distance >= RESYNC_DISTANCE_METERS
                        }
                    }
                    .collect { locationData ->
                        Log.d(TAG, "Significant location change detected, re-prioritizing regions")
                        val androidLocation = Location("").apply {
                            latitude = locationData.latitude
                            longitude = locationData.longitude
                        }
                        lastLocationUpdate = androidLocation
                        syncWithCurrentLocation(androidLocation)
                    }
            }
    }

    /**
     * Stop observing region changes and location updates.
     */
    fun stopObserving() {
        observerJob?.cancel()
        observerJob = null
        locationObserverJob?.cancel()
        locationObserverJob = null
    }

    /**
     * Sync geofences with the current list of regions.
     * Handles the 100 geofence limit by prioritizing nearby regions.
     */
    private suspend fun syncGeofences(
        regions: List<Region>,
        currentLocation: Location?
    ) {
        try {
            // Filter to only active regions with notifications enabled
            val activeRegions = regions.filter { it.notificationsEnabled }

            // Prioritize regions based on proximity if location is available
            val prioritizedRegions =
                if (currentLocation != null && activeRegions.size > GeofencingClientWrapper.MAX_GEOFENCES) {
                    prioritizeNearbyRegions(activeRegions, currentLocation)
                } else {
                    activeRegions
                }

            // Take only up to MAX_GEOFENCES
            val regionsToMonitor = prioritizedRegions.take(GeofencingClientWrapper.MAX_GEOFENCES)
            val newRegionIds = regionsToMonitor.map { it.id }.toSet()

            // Determine which geofences to add and remove
            val toAdd = regionsToMonitor.filter { it.id !in currentRegions }
            val toRemove = currentRegions.filter { it !in newRegionIds }

            // Remove old geofences
            if (toRemove.isNotEmpty()) {
                Log.d(TAG, "Removing ${toRemove.size} geofences")
                geofencingClientWrapper.removeGeofences(toRemove.toList())
            }

            // Add new geofences
            if (toAdd.isNotEmpty()) {
                Log.d(TAG, "Adding ${toAdd.size} geofences")
                geofencingClientWrapper.addGeofences(toAdd)
            }

            // Update current regions
            currentRegions = newRegionIds

            Log.i(TAG, "Synced geofences: ${currentRegions.size} active")

            // Log warning if we hit the limit
            if (activeRegions.size > GeofencingClientWrapper.MAX_GEOFENCES) {
                Log.w(
                    TAG,
                    "Region limit exceeded: ${activeRegions.size} regions, monitoring ${currentRegions.size}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing geofences", e)
        }
    }

    /**
     * Prioritize regions by distance from current location.
     * Returns regions sorted by distance (nearest first).
     */
    private fun prioritizeNearbyRegions(
        regions: List<Region>,
        currentLocation: Location
    ): List<Region> {
        val regionLocation =
            Location("").apply {
                latitude = 0.0
                longitude = 0.0
            }

        return regions.sortedBy { region ->
            regionLocation.latitude = region.latitude
            regionLocation.longitude = region.longitude
            currentLocation.distanceTo(regionLocation)
        }
    }

    /**
     * Manually trigger a sync with the current location.
     * Useful when location changes significantly.
     */
    suspend fun syncWithCurrentLocation(location: Location) {
        try {
            val regions =
                currentRegions.mapNotNull { regionId ->
                    regionRepository.getRegionById(regionId)
                }

            if (regions.isNotEmpty()) {
                syncGeofences(regions, location)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing with current location", e)
        }
    }

    /**
     * Force a complete resync of all geofences.
     */
    suspend fun forceResync(currentLocation: Location? = null) {
        try {
            Log.i(TAG, "Force resyncing geofences")

            // Remove all current geofences
            if (currentRegions.isNotEmpty()) {
                geofencingClientWrapper.removeGeofences(currentRegions.toList())
                currentRegions = emptySet()
            }

            // Re-fetch and sync regions
            val regions =
                currentRegions.mapNotNull { regionId ->
                    regionRepository.getRegionById(regionId)
                }

            syncGeofences(regions, currentLocation)
        } catch (e: Exception) {
            Log.e(TAG, "Error force resyncing geofences", e)
        }
    }

    companion object {
        private const val TAG = "RegionSyncObserver"

        /**
         * Minimum distance (in meters) that triggers a resync.
         * If the user moves this far, we should re-prioritize nearby regions.
         */
        const val RESYNC_DISTANCE_METERS = 5000.0 // 5 km
    }
}
