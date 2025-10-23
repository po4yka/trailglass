package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.LocalChanges
import com.po4yka.trailglass.data.remote.dto.PlaceVisitDto
import com.po4yka.trailglass.data.remote.dto.TripDto
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.sync.mapper.toDto
import com.po4yka.trailglass.data.sync.mapper.toDomain
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * Centralized manager for coordinating entity synchronization.
 *
 * Responsibilities:
 * - Collect local changes from all repositories
 * - Upload pending changes to server
 * - Download remote changes from server
 * - Resolve conflicts
 * - Update sync metadata
 */
@Inject
class SyncManager(
    private val syncCoordinator: SyncCoordinator,
    private val syncMetadataRepository: SyncMetadataRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val tripRepository: TripRepository,
    private val apiClient: TrailGlassApiClient,
    private val deviceId: String,
    private val userId: String
) {
    private val logger = logger()

    private val _syncProgress = MutableStateFlow<SyncProgress>(SyncProgress.Idle)
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()

    /**
     * Perform full synchronization of all entity types.
     */
    suspend fun performFullSync(): Result<SyncResult> {
        logger.info { "Starting full sync for user $userId on device $deviceId" }
        _syncProgress.value = SyncProgress.InProgress(0, "Collecting local changes...")

        return try {
            // 1. Collect local changes
            val localChanges = collectLocalChanges()

            logger.debug {
                "Collected local changes: " +
                        "${localChanges.placeVisits.size} place visits, " +
                        "${localChanges.trips.size} trips"
            }

            _syncProgress.value = SyncProgress.InProgress(30, "Uploading changes...")

            // 2. Perform delta sync
            val syncResult = syncCoordinator.performSync(deviceId, localChanges)

            syncResult.fold(
                onSuccess = { response ->
                    _syncProgress.value = SyncProgress.InProgress(60, "Downloading remote changes...")

                    // 3. Apply remote changes
                    applyRemoteChanges(response.remoteChanges)

                    _syncProgress.value = SyncProgress.InProgress(80, "Updating sync metadata...")

                    // 4. Update sync metadata for accepted entities
                    updateSyncMetadata(response)

                    _syncProgress.value = SyncProgress.InProgress(90, "Handling conflicts...")

                    // 5. Handle conflicts
                    val conflictsResolved = handleConflicts(response.conflicts)

                    _syncProgress.value = SyncProgress.Completed(
                        SyncResult(
                            uploaded = response.accepted.placeVisits.size + response.accepted.trips.size,
                            downloaded = response.remoteChanges.placeVisits.size + response.remoteChanges.trips.size,
                            conflicts = response.conflicts.size,
                            conflictsResolved = conflictsResolved,
                            errors = response.rejected.placeVisits.size + response.rejected.trips.size
                        )
                    )

                    logger.info {
                        "Sync completed successfully: " +
                                "uploaded ${response.accepted.placeVisits.size + response.accepted.trips.size}, " +
                                "downloaded ${response.remoteChanges.placeVisits.size + response.remoteChanges.trips.size}, " +
                                "${response.conflicts.size} conflicts"
                    }

                    Result.success(
                        SyncResult(
                            uploaded = response.accepted.placeVisits.size + response.accepted.trips.size,
                            downloaded = response.remoteChanges.placeVisits.size + response.remoteChanges.trips.size,
                            conflicts = response.conflicts.size,
                            conflictsResolved = conflictsResolved,
                            errors = response.rejected.placeVisits.size + response.rejected.trips.size
                        )
                    )
                },
                onFailure = { error ->
                    logger.error(error) { "Sync failed" }
                    _syncProgress.value = SyncProgress.Failed(error.message ?: "Unknown error")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during sync" }
            _syncProgress.value = SyncProgress.Failed(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    /**
     * Collect local changes that need to be synced.
     */
    private suspend fun collectLocalChanges(): LocalChanges {
        val placeVisits = mutableListOf<PlaceVisitDto>()
        val trips = mutableListOf<TripDto>()

        // Collect pending place visits
        val pendingPlaceVisits = syncMetadataRepository.getPendingSync(EntityType.PLACE_VISIT)
        for (metadata in pendingPlaceVisits) {
            val visit = placeVisitRepository.getVisitById(metadata.entityId)
            if (visit != null) {
                placeVisits.add(
                    visit.toDto(
                        localVersion = metadata.localVersion,
                        serverVersion = metadata.serverVersion,
                        deviceId = metadata.deviceId
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

        return LocalChanges(
            locations = emptyList(), // TODO: Implement location sync
            placeVisits = placeVisits,
            trips = trips,
            photos = emptyList(), // TODO: Implement photo sync
            settings = null // TODO: Implement settings sync
        )
    }

    /**
     * Apply remote changes to local database.
     */
    private suspend fun applyRemoteChanges(remoteChanges: com.po4yka.trailglass.data.remote.dto.RemoteChanges) {
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
                tripRepository.insertTrip(trip)

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
            syncMetadataRepository.deletemetadata(deletedId, EntityType.PLACE_VISIT)
        }

        for (deletedId in remoteChanges.deletedIds.trips) {
            tripRepository.deleteTrip(deletedId)
            syncMetadataRepository.deletemetadata(deletedId, EntityType.TRIP)
        }
    }

    /**
     * Update sync metadata for successfully synced entities.
     */
    private suspend fun updateSyncMetadata(response: com.po4yka.trailglass.data.remote.dto.DeltaSyncResponse) {
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

    /**
     * Handle sync conflicts.
     * For now, uses automatic resolution. Manual resolution can be added later.
     */
    private suspend fun handleConflicts(
        conflicts: List<com.po4yka.trailglass.data.remote.dto.SyncConflictDto>
    ): Int {
        var resolved = 0

        for (conflict in conflicts) {
            try {
                // For now, use suggested resolution (usually KEEP_REMOTE for server authority)
                // In a full implementation, this would present conflicts to the user

                logger.warn {
                    "Conflict detected for ${conflict.entityType}:${conflict.entityId}. " +
                            "Using automatic resolution: ${conflict.suggestedResolution}"
                }

                // TODO: Implement proper conflict resolution UI
                // For now, just log the conflict
                resolved++
            } catch (e: Exception) {
                logger.error(e) { "Failed to resolve conflict for ${conflict.entityId}" }
            }
        }

        return resolved
    }

    /**
     * Mark an entity as needing sync.
     */
    suspend fun markForSync(entityId: String, entityType: EntityType) {
        val existing = syncMetadataRepository.getMetadata(entityId, entityType)

        val metadata = if (existing != null) {
            existing.copy(
                isPendingSync = true,
                lastModified = Clock.System.now(),
                localVersion = existing.localVersion + 1
            )
        } else {
            SyncMetadata(
                entityId = entityId,
                entityType = entityType,
                lastModified = Clock.System.now(),
                deviceId = deviceId
            )
        }

        syncMetadataRepository.upsertMetadata(metadata)
        syncCoordinator.markSyncNeeded()
    }

    /**
     * Get count of pending sync items.
     */
    suspend fun getPendingSyncCount(): Int {
        return syncMetadataRepository.getPendingSyncCount()
    }

    /**
     * Get current sync status for UI display.
     */
    suspend fun getSyncStatus(): SyncStatusUiModel {
        val pendingCount = getPendingSyncCount()
        val lastSyncMetadata = syncMetadataRepository.getLastSyncedMetadata()

        return SyncStatusUiModel(
            isActive = _syncProgress.value is SyncProgress.InProgress,
            progress = _syncProgress.value,
            lastSyncTime = lastSyncMetadata?.lastSynced,
            pendingCount = pendingCount,
            conflictCount = 0, // TODO: Implement conflict count
            lastError = (syncProgress.value as? SyncProgress.Failed)?.error
        )
    }

    /**
     * Get list of unresolved conflicts for UI.
     */
    suspend fun getUnresolvedConflicts(): List<ConflictUiModel> {
        // TODO: Implement conflict storage and retrieval
        return emptyList()
    }

    /**
     * Resolve a conflict with the given choice.
     */
    suspend fun resolveConflict(
        conflictId: String,
        choice: ConflictResolutionChoice
    ): Result<Unit> {
        // TODO: Implement conflict resolution
        return Result.success(Unit)
    }
}

/**
 * Sync progress state.
 */
sealed class SyncProgress {
    data object Idle : SyncProgress()
    data class InProgress(val percentage: Int, val message: String) : SyncProgress()
    data class Completed(val result: SyncResult) : SyncProgress()
    data class Failed(val error: String) : SyncProgress()
}

/**
 * Sync result summary.
 */
data class SyncResult(
    val uploaded: Int,
    val downloaded: Int,
    val conflicts: Int,
    val conflictsResolved: Int,
    val errors: Int
)
