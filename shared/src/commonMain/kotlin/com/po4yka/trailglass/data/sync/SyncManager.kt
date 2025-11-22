package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkState
import com.po4yka.trailglass.data.network.allowsSync
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.LocalChanges
import com.po4yka.trailglass.data.remote.dto.PlaceVisitDto
import com.po4yka.trailglass.data.remote.dto.TripDto
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.sync.mapper.toDto
import com.po4yka.trailglass.data.sync.mapper.toDomain
import com.po4yka.trailglass.data.sync.mapper.toMetadataDto
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
 * - Monitor network connectivity and auto-sync when online
 *
 * IMPORTANT: Call [cleanup] when SyncManager is no longer needed to release resources.
 */
@Inject
class SyncManager(
    private val syncCoordinator: SyncCoordinator,
    private val syncMetadataRepository: SyncMetadataRepository,
    private val conflictRepository: ConflictRepository,
    private val networkMonitor: NetworkConnectivityMonitor,
    private val placeVisitRepository: PlaceVisitRepository,
    private val tripRepository: TripRepository,
    private val locationRepository: LocationRepository,
    private val photoRepository: PhotoRepository,
    private val settingsRepository: SettingsRepository,
    private val apiClient: TrailGlassApiClient,
    private val deviceId: String,
    private val userId: String
) {
    private val logger = logger()
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(supervisorJob + Dispatchers.Default)

    private val _syncProgress = MutableStateFlow<SyncProgress>(SyncProgress.Idle)
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()

    private var shouldAutoSyncWhenOnline = false

    init {
        // Start network monitoring
        networkMonitor.startMonitoring()

        // Listen for network state changes
        scope.launch {
            networkMonitor.networkState.collect { state ->
                handleNetworkStateChange(state)
            }
        }
    }

    /**
     * Handle network state changes and auto-trigger sync when online.
     */
    private fun handleNetworkStateChange(state: NetworkState) {
        logger.info { "Network state changed: $state" }

        when (state) {
            is NetworkState.Connected -> {
                if (shouldAutoSyncWhenOnline) {
                    logger.info { "Network became available, triggering auto-sync" }
                    scope.launch {
                        performFullSync()
                    }
                    shouldAutoSyncWhenOnline = false
                }
            }
            is NetworkState.Disconnected -> {
                logger.warn { "Network disconnected" }
            }
            is NetworkState.Limited -> {
                logger.warn { "Network limited: ${state.reason}" }
            }
        }
    }

    /**
     * Perform full synchronization of all entity types.
     */
    suspend fun performFullSync(): Result<SyncResultSummary> {
        // Skip sync for guest users (no account, local-only mode)
        if (userId == UserSession.GUEST_USER_ID) {
            logger.debug { "Skipping sync: user is in guest mode (local-only)" }
            _syncProgress.value = SyncProgress.Idle
            return Result.success(
                SyncResultSummary(
                    uploaded = 0,
                    downloaded = 0,
                    conflicts = 0,
                    conflictsResolved = 0,
                    errors = 0
                )
            )
        }

        // Check network connectivity
        if (!networkMonitor.networkState.value.allowsSync()) {
            logger.warn { "Cannot sync: network not available" }
            shouldAutoSyncWhenOnline = true
            _syncProgress.value = SyncProgress.Failed("No network connection")
            return Result.failure(Exception("No network connection"))
        }

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
                        SyncResultSummary(
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
                        SyncResultSummary(
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
            settingsDto = settings.toDto(
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
     * Stores conflicts for manual resolution through UI.
     */
    private suspend fun handleConflicts(
        conflicts: List<com.po4yka.trailglass.data.remote.dto.SyncConflictDto>
    ): Int {
        var resolved = 0

        for (conflict in conflicts) {
            try {
                // Check if suggested resolution is automatic
                if (conflict.suggestedResolution == com.po4yka.trailglass.data.remote.dto.ConflictResolution.MERGE ||
                    conflict.suggestedResolution == com.po4yka.trailglass.data.remote.dto.ConflictResolution.KEEP_REMOTE) {
                    // Apply automatic resolution
                    val resolution = when (conflict.suggestedResolution) {
                        com.po4yka.trailglass.data.remote.dto.ConflictResolution.KEEP_REMOTE -> ConflictResolutionChoice.KEEP_REMOTE
                        com.po4yka.trailglass.data.remote.dto.ConflictResolution.MERGE -> ConflictResolutionChoice.MERGE
                        else -> ConflictResolutionChoice.KEEP_REMOTE
                    }

                    resolveConflictAutomatically(conflict, resolution)
                    resolved++

                    logger.info {
                        "Automatically resolved conflict for ${conflict.entityType}:${conflict.entityId} " +
                                "using ${conflict.suggestedResolution}"
                    }
                } else {
                    // Store conflict for manual resolution
                    conflictRepository.storeConflict(conflict.toStoredConflict())

                    logger.warn {
                        "Conflict stored for manual resolution: ${conflict.entityType}:${conflict.entityId}"
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to handle conflict for ${conflict.entityId}" }
            }
        }

        return resolved
    }

    /**
     * Automatically resolve a conflict without user interaction.
     */
    private suspend fun resolveConflictAutomatically(
        conflict: com.po4yka.trailglass.data.remote.dto.SyncConflictDto,
        choice: ConflictResolutionChoice
    ) {
        when (choice) {
            ConflictResolutionChoice.KEEP_REMOTE -> {
                // Remote wins - data will be applied in applyRemoteChanges
                // Just mark local as synced with remote version
                val entityType = when (conflict.entityType) {
                    com.po4yka.trailglass.data.remote.dto.EntityType.PLACE_VISIT -> EntityType.PLACE_VISIT
                    com.po4yka.trailglass.data.remote.dto.EntityType.TRIP -> EntityType.TRIP
                    else -> return
                }

                syncMetadataRepository.markAsSynced(
                    entityId = conflict.entityId,
                    entityType = entityType,
                    serverVersion = conflict.remoteVersion["version"]?.toLongOrNull() ?: 0L
                )
            }
            ConflictResolutionChoice.KEEP_LOCAL -> {
                // Local wins - force upload local version
                // Mark for re-sync
                val entityType = when (conflict.entityType) {
                    com.po4yka.trailglass.data.remote.dto.EntityType.PLACE_VISIT -> EntityType.PLACE_VISIT
                    com.po4yka.trailglass.data.remote.dto.EntityType.TRIP -> EntityType.TRIP
                    else -> return
                }

                val metadata = syncMetadataRepository.getMetadata(conflict.entityId, entityType)
                metadata?.let {
                    syncMetadataRepository.upsertMetadata(
                        it.copy(isPendingSync = true)
                    )
                }
            }
            ConflictResolutionChoice.MERGE -> {
                // Merge resolution - use last-write-wins strategy
                // In a more sophisticated implementation, this would merge fields
                val remoteVer = conflict.remoteVersion["version"]?.toLongOrNull() ?: 0L
                val localVer = conflict.localVersion["version"]?.toLongOrNull() ?: 0L
                if (remoteVer > localVer) {
                    resolveConflictAutomatically(conflict, ConflictResolutionChoice.KEEP_REMOTE)
                } else {
                    resolveConflictAutomatically(conflict, ConflictResolutionChoice.KEEP_LOCAL)
                }
            }
            ConflictResolutionChoice.MANUAL -> {
                // Should not happen in automatic resolution
                // Store for manual resolution
                conflictRepository.storeConflict(conflict.toStoredConflict())
            }
        }
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
        val conflictCount = conflictRepository.getConflictCount()
        val lastSyncMetadata = syncMetadataRepository.getLastSyncedMetadata()
        val networkInfo = networkMonitor.networkInfo.value

        return SyncStatusUiModel(
            isActive = _syncProgress.value is SyncProgress.InProgress,
            progress = _syncProgress.value,
            lastSyncTime = lastSyncMetadata?.lastSynced,
            pendingCount = pendingCount,
            conflictCount = conflictCount,
            lastError = (syncProgress.value as? SyncProgress.Failed)?.error,
            networkState = networkInfo.state,
            networkType = networkInfo.type,
            isNetworkMetered = networkInfo.isMetered
        )
    }

    /**
     * Get list of unresolved conflicts for UI.
     */
    suspend fun getUnresolvedConflicts(): List<ConflictUiModel> {
        return conflictRepository.getPendingConflicts().map { conflict ->
            convertToUiModel(conflict)
        }
    }

    /**
     * Convert StoredConflict to UI-friendly ConflictUiModel.
     */
    private suspend fun convertToUiModel(conflict: StoredConflict): ConflictUiModel {
        // Get entity name based on type
        val entityName = when (conflict.entityType) {
            EntityType.PLACE_VISIT -> {
                placeVisitRepository.getVisitById(conflict.entityId)?.displayName ?: conflict.entityId
            }
            EntityType.TRIP -> {
                tripRepository.getTripById(conflict.entityId)?.name ?: conflict.entityId
            }
            else -> conflict.entityId
        }

        return ConflictUiModel(
            conflictId = conflict.conflictId,
            entityType = conflict.entityType,
            entityId = conflict.entityId,
            entityName = entityName,
            localVersion = conflict.localVersion,
            remoteVersion = conflict.remoteVersion,
            localModified = conflict.createdAt, // Approximation - could be improved
            remoteModified = conflict.createdAt,
            conflictDescription = buildConflictDescription(conflict),
            localPreview = formatDataPreview(conflict.localData),
            remotePreview = formatDataPreview(conflict.remoteData)
        )
    }

    private fun buildConflictDescription(conflict: StoredConflict): String {
        val fields = conflict.conflictedFields.joinToString(", ")
        return "Conflicting changes in: $fields. " +
               "Local version ${conflict.localVersion} vs Remote version ${conflict.remoteVersion}."
    }

    private fun formatDataPreview(data: String): String {
        // Format JSON or data string for display
        // Limit to 200 characters for preview
        return if (data.length > 200) {
            data.take(197) + "..."
        } else {
            data
        }
    }

    /**
     * Resolve a conflict with the given choice.
     */
    suspend fun resolveConflict(
        conflictId: String,
        choice: ConflictResolutionChoice
    ): Result<Unit> {
        return try {
            val conflict = conflictRepository.getConflict(conflictId)
                ?: return Result.failure(Exception("Conflict not found: $conflictId"))

            logger.info { "Resolving conflict $conflictId with choice $choice" }

            when (choice) {
                ConflictResolutionChoice.KEEP_LOCAL -> {
                    // Local wins - mark entity for re-upload
                    val metadata = syncMetadataRepository.getMetadata(
                        conflict.entityId,
                        conflict.entityType
                    )
                    metadata?.let {
                        syncMetadataRepository.upsertMetadata(
                            it.copy(
                                isPendingSync = true,
                                localVersion = it.localVersion + 1
                            )
                        )
                    }
                }
                ConflictResolutionChoice.KEEP_REMOTE -> {
                    // Remote wins - fetch and apply remote version
                    // Mark local as synced with remote version
                    syncMetadataRepository.markAsSynced(
                        entityId = conflict.entityId,
                        entityType = conflict.entityType,
                        serverVersion = conflict.remoteVersion
                    )

                    // Trigger a sync to get the remote data
                    syncCoordinator.markSyncNeeded()
                }
                ConflictResolutionChoice.MERGE -> {
                    // Merge using last-write-wins
                    if (conflict.remoteVersion > conflict.localVersion) {
                        resolveConflict(conflictId, ConflictResolutionChoice.KEEP_REMOTE)
                    } else {
                        resolveConflict(conflictId, ConflictResolutionChoice.KEEP_LOCAL)
                    }
                    return Result.success(Unit)
                }
                ConflictResolutionChoice.MANUAL -> {
                    // User needs to manually resolve - keep conflict stored
                    return Result.failure(Exception("Manual resolution not implemented"))
                }
            }

            // Mark conflict as resolved
            conflictRepository.markAsResolved(conflictId)

            logger.info { "Conflict $conflictId resolved successfully with $choice" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to resolve conflict $conflictId" }
            Result.failure(e)
        }
    }

    /**
     * Cleanup method to release resources.
     * MUST be called when SyncManager is no longer needed to prevent memory leaks.
     *
     * This method:
     * - Stops network monitoring
     * - Cancels all running coroutines
     * - Releases the supervisor job
     */
    fun cleanup() {
        logger.info { "Cleaning up SyncManager" }

        // Stop network monitoring
        networkMonitor.stopMonitoring()

        // Cancel all coroutines in this scope
        supervisorJob.cancel()

        logger.debug { "SyncManager cleanup complete" }
    }
}

/**
 * Sync progress state.
 */
sealed class SyncProgress {
    data object Idle : SyncProgress()
    data class InProgress(val percentage: Int, val message: String) : SyncProgress()
    data class Completed(val result: SyncResultSummary) : SyncProgress()
    data class Failed(val error: String) : SyncProgress()
}

/**
 * Sync result summary.
 */
data class SyncResultSummary(
    val uploaded: Int,
    val downloaded: Int,
    val conflicts: Int,
    val conflictsResolved: Int,
    val errors: Int
)
