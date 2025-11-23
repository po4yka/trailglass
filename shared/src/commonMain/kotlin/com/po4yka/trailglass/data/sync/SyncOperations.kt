package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.remote.dto.LocalChanges
import com.po4yka.trailglass.data.remote.dto.PlaceVisitDto
import com.po4yka.trailglass.data.remote.dto.TripDto
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.sync.mapper.toDomain
import com.po4yka.trailglass.data.sync.mapper.toDto
import com.po4yka.trailglass.data.sync.mapper.toMetadataDto
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock

/**
 * Core synchronization operations handler.
 *
 * Responsibilities:
 * - Collect local changes from all repositories
 * - Apply remote changes to local database
 * - Update sync metadata after successful syncs
 */
internal class SyncOperations(
    private val syncMetadataRepository: SyncMetadataRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val tripRepository: TripRepository,
    private val locationRepository: LocationRepository,
    private val photoRepository: PhotoRepository,
    private val settingsRepository: SettingsRepository,
    private val deviceId: String
) {
    private val logger = logger()

    /**
     * Collect local changes that need to be synced.
     */
    suspend fun collectLocalChanges(): LocalChanges {
        val placeVisits = mutableListOf<PlaceVisitDto>()
        val trips = mutableListOf<TripDto>()
        val locations = mutableListOf<com.po4yka.trailglass.data.remote.dto.LocationDto>()
        val photos = mutableListOf<com.po4yka.trailglass.data.remote.dto.PhotoMetadataDto>()
        var settingsDto: com.po4yka.trailglass.data.remote.dto.SettingsDto? = null

        // Collect pending place visits
        val pendingPlaceVisits = syncMetadataRepository.getPendingSync(EntityType.PLACE_VISIT)
        for (metadata in pendingPlaceVisits) {
            val visit = placeVisitRepository.getVisitById(metadata.entityId)
            if (visit != null) {
                // Get photos attached to this visit
                val visitPhotos = photoRepository.getPhotosForVisit(visit.id)
                val photoIds = visitPhotos.map { it.id }

                placeVisits.add(
                    visit.toDto(
                        localVersion = metadata.localVersion,
                        serverVersion = metadata.serverVersion,
                        deviceId = metadata.deviceId,
                        photoIds = photoIds,
                        tripId = null // Trip relationship not stored in current schema
                    )
                )
            }
        }

        // Collect pending trips
        val pendingTrips = syncMetadataRepository.getPendingSync(EntityType.TRIP)
        for (metadata in pendingTrips) {
            val trip = tripRepository.getTripById(metadata.entityId)
            if (trip != null) {
                trips.add(
                    trip.toDto(
                        localVersion = metadata.localVersion,
                        serverVersion = metadata.serverVersion,
                        deviceId = metadata.deviceId
                    )
                )
            }
        }

        // Collect pending location samples
        val pendingLocations = syncMetadataRepository.getPendingSync(EntityType.LOCATION_SAMPLE)
        for (metadata in pendingLocations) {
            val location = locationRepository.getSampleById(metadata.entityId)
            location.onSuccess { sample ->
                if (sample != null) {
                    locations.add(
                        sample.toDto(
                            localVersion = metadata.localVersion,
                            serverVersion = metadata.serverVersion,
                            deviceId = metadata.deviceId
                        )
                    )
                }
            }
        }

        // Collect pending photos
        val pendingPhotos = syncMetadataRepository.getPendingSync(EntityType.PHOTO)
        for (metadata in pendingPhotos) {
            val photo = photoRepository.getPhotoById(metadata.entityId)
            if (photo != null) {
                photos.add(
                    photo.toMetadataDto(
                        localVersion = metadata.localVersion,
                        serverVersion = metadata.serverVersion,
                        deviceId = metadata.deviceId
                    )
                )
            }
        }

        // Collect settings if pending
        val pendingSettings = syncMetadataRepository.getPendingSync(EntityType.SETTINGS)
        if (pendingSettings.isNotEmpty()) {
            val settings = settingsRepository.getCurrentSettings()
            val metadata = pendingSettings.first()
            settingsDto =
                settings.toDto(
                    serverVersion = metadata.serverVersion,
                    lastModified = metadata.lastModified
                )
        }

        return LocalChanges(
            locations = locations,
            placeVisits = placeVisits,
            trips = trips,
            photos = photos,
            settings = settingsDto
        )
    }

    /**
     * Apply remote changes to local database.
     */
    suspend fun applyRemoteChanges(remoteChanges: com.po4yka.trailglass.data.remote.dto.RemoteChanges) {
        // Apply place visit changes
        for (visitDto in remoteChanges.placeVisits) {
            try {
                val visit = visitDto.toDomain()
                placeVisitRepository.insertVisit(visit)

                // Update sync metadata
                syncMetadataRepository.upsertMetadata(
                    SyncMetadata(
                        entityId = visit.id,
                        entityType = EntityType.PLACE_VISIT,
                        serverVersion = visitDto.serverVersion ?: 1,
                        lastModified = Clock.System.now(),
                        lastSynced = Clock.System.now(),
                        isPendingSync = false,
                        deviceId = deviceId
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to apply remote place visit ${visitDto.id}" }
            }
        }

        // Apply trip changes
        for (tripDto in remoteChanges.trips) {
            try {
                val trip = tripDto.toDomain()
                tripRepository.upsertTrip(trip)

                // Update sync metadata
                syncMetadataRepository.upsertMetadata(
                    SyncMetadata(
                        entityId = trip.id,
                        entityType = EntityType.TRIP,
                        serverVersion = tripDto.serverVersion ?: 1,
                        lastModified = Clock.System.now(),
                        lastSynced = Clock.System.now(),
                        isPendingSync = false,
                        deviceId = deviceId
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to apply remote trip ${tripDto.id}" }
            }
        }

        // Handle deletions
        for (deletedId in remoteChanges.deletedIds.placeVisits) {
            placeVisitRepository.deleteVisit(deletedId)
            syncMetadataRepository.deleteMetadata(deletedId, EntityType.PLACE_VISIT)
        }

        for (deletedId in remoteChanges.deletedIds.trips) {
            tripRepository.deleteTrip(deletedId)
            syncMetadataRepository.deleteMetadata(deletedId, EntityType.TRIP)
        }
    }

    /**
     * Update sync metadata for successfully synced entities.
     */
    suspend fun updateSyncMetadata(response: com.po4yka.trailglass.data.remote.dto.DeltaSyncResponse) {
        // Mark accepted place visits as synced
        for (visitId in response.accepted.placeVisits) {
            syncMetadataRepository.markAsSynced(
                entityId = visitId,
                entityType = EntityType.PLACE_VISIT,
                serverVersion = response.syncVersion
            )
        }

        // Mark accepted trips as synced
        for (tripId in response.accepted.trips) {
            syncMetadataRepository.markAsSynced(
                entityId = tripId,
                entityType = EntityType.TRIP,
                serverVersion = response.syncVersion
            )
        }

        // Mark rejected entities with error
        for (rejection in response.rejected.placeVisits) {
            syncMetadataRepository.markSyncFailed(
                entityId = rejection.id,
                entityType = EntityType.PLACE_VISIT,
                error = rejection.reason
            )
        }

        for (rejection in response.rejected.trips) {
            syncMetadataRepository.markSyncFailed(
                entityId = rejection.id,
                entityType = EntityType.TRIP,
                error = rejection.reason
            )
        }
    }
}
