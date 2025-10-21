package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.data.local.dao.LocationDao
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.BatchLocationRequest
import com.po4yka.trailglass.data.remote.dto.LocationDto
import com.po4yka.trailglass.data.remote.dto.SyncAction
import com.po4yka.trailglass.data.sync.SyncCoordinator
import com.po4yka.trailglass.domain.model.Location
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Offline-first repository for locations with automatic sync.
 */
@Inject
class SyncableLocationRepository(
    private val locationDao: LocationDao,
    private val apiClient: TrailGlassApiClient,
    private val syncCoordinator: SyncCoordinator,
    private val deviceId: String
) {
    private val logger = logger()

    /**
     * Get locations from local database.
     * This is the single source of truth for the UI.
     */
    fun getLocations(
        startTime: Instant,
        endTime: Instant
    ): Flow<List<Location>> {
        return locationDao.getLocationsBetween(startTime, endTime)
    }

    /**
     * Save a location locally and mark it for sync.
     */
    suspend fun saveLocation(location: Location) {
        logger.debug { "Saving location ${location.id} to local database" }

        // Save to local database with pending sync flag
        locationDao.insert(location.copy(isPendingSync = true))

        // Mark sync as needed
        syncCoordinator.markSyncNeeded()

        // Optionally trigger immediate sync if connected
        // This could be controlled by settings
    }

    /**
     * Save multiple locations locally.
     */
    suspend fun saveLocations(locations: List<Location>) {
        logger.debug { "Saving ${locations.size} locations to local database" }

        // Mark all as pending sync
        val pendingLocations = locations.map { it.copy(isPendingSync = true) }
        locationDao.insertAll(pendingLocations)

        // Mark sync as needed
        syncCoordinator.markSyncNeeded()
    }

    /**
     * Get pending locations that need to be synced.
     */
    suspend fun getPendingLocations(): List<Location> {
        return locationDao.getPendingSync()
    }

    /**
     * Upload locations to server.
     */
    suspend fun uploadPendingLocations(): Result<Int> {
        val pendingLocations = getPendingLocations()

        if (pendingLocations.isEmpty()) {
            logger.debug { "No pending locations to upload" }
            return Result.success(0)
        }

        logger.info { "Uploading ${pendingLocations.size} pending locations" }

        val locationDtos = pendingLocations.map { location ->
            LocationDto(
                id = location.id,
                timestamp = location.timestamp.toString(),
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                accuracy = location.accuracy,
                speed = location.speed,
                bearing = location.bearing,
                provider = location.provider,
                batteryLevel = location.batteryLevel,
                clientTimestamp = location.timestamp.toString(),
                syncAction = SyncAction.CREATE,
                localVersion = location.localVersion,
                serverVersion = location.serverVersion,
                deviceId = deviceId
            )
        }

        val request = BatchLocationRequest(
            locations = locationDtos,
            deviceId = deviceId
        )

        return apiClient.uploadLocationBatch(request)
            .map { response ->
                logger.info {
                    "Upload complete: ${response.accepted} accepted, " +
                            "${response.rejected} rejected, ${response.duplicates} duplicates"
                }

                // Mark accepted locations as synced
                if (response.accepted > 0) {
                    val acceptedIds = pendingLocations.take(response.accepted).map { it.id }
                    locationDao.markAsSynced(acceptedIds, response.syncVersion)
                }

                response.accepted
            }
    }

    /**
     * Sync locations with server (pull remote changes).
     */
    suspend fun syncFromServer(
        startTime: Instant? = null,
        endTime: Instant? = null
    ): Result<Int> {
        logger.info { "Syncing locations from server" }

        return apiClient.getLocations(
            startTime = startTime?.toString(),
            endTime = endTime?.toString(),
            limit = 10000
        ).map { response ->
            logger.info { "Received ${response.locations.size} locations from server" }

            // Convert DTOs to domain models
            val locations = response.locations.map { dto ->
                Location(
                    id = dto.id,
                    timestamp = Instant.parse(dto.timestamp),
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    altitude = dto.altitude,
                    accuracy = dto.accuracy,
                    speed = dto.speed,
                    bearing = dto.bearing,
                    provider = dto.provider,
                    batteryLevel = dto.batteryLevel,
                    localVersion = 1,
                    serverVersion = dto.serverVersion,
                    lastModified = Instant.parse(dto.timestamp),
                    deviceId = dto.deviceId ?: deviceId,
                    isDeleted = false,
                    isPendingSync = false
                )
            }

            // Upsert into local database
            locationDao.upsertAll(locations)

            locations.size
        }
    }

    /**
     * Delete a location (soft delete, mark for sync).
     */
    suspend fun deleteLocation(locationId: String) {
        logger.debug { "Soft deleting location $locationId" }

        locationDao.markAsDeleted(locationId)
        syncCoordinator.markSyncNeeded()
    }

    /**
     * Clear all local locations (use with caution).
     */
    suspend fun clearAllLocations() {
        logger.warn { "Clearing all local locations" }
        locationDao.deleteAll()
    }
}
