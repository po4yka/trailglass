package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.network.NetworkConnectivityMonitor
import com.po4yka.trailglass.data.network.NetworkState
import com.po4yka.trailglass.data.network.allowsSync
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.sync.SyncUiState.ConflictResolutionChoice
import com.po4yka.trailglass.data.sync.SyncUiState.ConflictUiModel
import com.po4yka.trailglass.data.sync.SyncUiState.SyncStatusUiModel
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * Centralized manager for coordinating entity synchronization.
 *
 * Responsibilities:
 * - Coordinate sync operations through delegated components
 * - Monitor network connectivity and auto-sync when online
 * - Provide sync status and progress tracking
 * - Expose conflict resolution interface for UI
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

    // Delegated components for core operations
    private val syncOperations =
        SyncOperations(
            syncMetadataRepository = syncMetadataRepository,
            placeVisitRepository = placeVisitRepository,
            tripRepository = tripRepository,
            locationRepository = locationRepository,
            photoRepository = photoRepository,
            settingsRepository = settingsRepository,
            deviceId = deviceId
        )

    private val conflictResolver =
        SyncConflictResolver(
            conflictRepository = conflictRepository,
            syncMetadataRepository = syncMetadataRepository,
            syncCoordinator = syncCoordinator,
            placeVisitRepository = placeVisitRepository,
            tripRepository = tripRepository
        )

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

    /** Handle network state changes and auto-trigger sync when online. */
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

    /** Perform full synchronization of all entity types. */
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
            val localChanges = syncOperations.collectLocalChanges()

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
                    syncOperations.applyRemoteChanges(response.remoteChanges)

                    _syncProgress.value = SyncProgress.InProgress(80, "Updating sync metadata...")

                    // 4. Update sync metadata for accepted entities
                    syncOperations.updateSyncMetadata(response)

                    _syncProgress.value = SyncProgress.InProgress(90, "Handling conflicts...")

                    // 5. Handle conflicts
                    val conflictsResolved = conflictResolver.handleConflicts(response.conflicts)

                    _syncProgress.value =
                        SyncProgress.Completed(
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

    /** Mark an entity as needing sync. */
    suspend fun markForSync(
        entityId: String,
        entityType: EntityType
    ) {
        val existing = syncMetadataRepository.getMetadata(entityId, entityType)

        val metadata =
            if (existing != null) {
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

    /** Get count of pending sync items. */
    suspend fun getPendingSyncCount(): Int = syncMetadataRepository.getPendingSyncCount()

    /** Get current sync status for UI display. */
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

    /** Get list of unresolved conflicts for UI. */
    suspend fun getUnresolvedConflicts(): List<ConflictUiModel> =
        conflictRepository.getPendingConflicts().map { conflict ->
            conflictResolver.convertToUiModel(conflict)
        }

    /** Resolve a conflict with the given choice. */
    suspend fun resolveConflict(
        conflictId: String,
        choice: ConflictResolutionChoice
    ): Result<Unit> = conflictResolver.resolveConflict(conflictId, choice)

    /**
     * Cleanup method to release resources. MUST be called when SyncManager is no longer needed to prevent memory leaks.
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
