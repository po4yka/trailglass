package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.*
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Repository for storing sync state.
 */
interface SyncStateRepository {
    suspend fun getSyncState(): SyncState

    suspend fun updateSyncState(state: SyncState)
}

/**
 * Coordinates synchronization between local and remote data.
 */
@Inject
class SyncCoordinator(
    private val apiClient: TrailGlassApiClient,
    private val syncStateRepository: SyncStateRepository
) {
    private val logger = logger()

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        // Load initial sync state
        // Note: This should be called after construction in a coroutine scope
    }

    suspend fun initialize() {
        val savedState = syncStateRepository.getSyncState()
        _syncState.value = savedState
    }

    /**
     * Perform full delta synchronization.
     */
    suspend fun performSync(
        deviceId: String,
        localChanges: LocalChanges
    ): Result<DeltaSyncResponse> {
        if (_syncState.value.isSyncing) {
            logger.warn { "Sync already in progress" }
            return Result.failure(Exception("Sync already in progress"))
        }

        updateSyncingState(true, null)

        val startTime = Clock.System.now()

        return try {
            val request =
                DeltaSyncRequest(
                    deviceId = deviceId,
                    lastSyncVersion = _syncState.value.lastSyncVersion,
                    localChanges = localChanges
                )

            logger.info {
                "Starting delta sync from version ${request.lastSyncVersion} " +
                    "with ${countLocalChanges(localChanges)} local changes"
            }

            val result = apiClient.performDeltaSync(request)

            result.onSuccess { response ->
                logger.info {
                    "Sync completed successfully to version ${response.syncVersion}. " +
                        "Conflicts: ${response.conflicts.size}, " +
                        "Accepted: ${countAccepted(response.accepted)}, " +
                        "Rejected: ${countRejected(response.rejected)}"
                }

                // Update sync state
                val newState =
                    _syncState.value.copy(
                        lastSyncTimestamp = Instant.parse(response.syncTimestamp),
                        lastSyncVersion = response.syncVersion,
                        isSyncing = false,
                        pendingChanges = response.conflicts.size,
                        error = null
                    )

                _syncState.value = newState
                syncStateRepository.updateSyncState(newState)
            }

            result.onFailure { error ->
                logger.error(error) { "Sync failed" }
                updateSyncingState(false, error.message)
            }

            result
        } catch (e: Exception) {
            logger.error(e) { "Unexpected sync error" }
            updateSyncingState(false, e.message)
            Result.failure(e)
        }
    }

    /**
     * Resolve a specific conflict.
     */
    suspend fun resolveConflict(
        conflictId: String,
        resolution: ConflictResolution,
        resolvedEntity: Map<String, String>
    ): Result<ResolveConflictResponse> {
        logger.info { "Resolving conflict $conflictId with strategy $resolution" }

        val request =
            ResolveConflictRequest(
                conflictId = conflictId,
                resolution = resolution,
                resolvedEntity = resolvedEntity
            )

        val result = apiClient.resolveConflict(request)

        result.onSuccess { response ->
            logger.info {
                "Conflict resolved successfully. New version: ${response.syncVersion}"
            }

            // Update sync state
            val currentPending = _syncState.value.pendingChanges
            if (currentPending > 0) {
                _syncState.update {
                    it.copy(
                        lastSyncVersion = response.syncVersion,
                        pendingChanges = currentPending - 1
                    )
                }
            }
        }

        return result
    }

    /**
     * Get current sync status from server.
     */
    suspend fun refreshSyncStatus(): Result<SyncStatusResponse> {
        logger.debug { "Refreshing sync status from server" }

        val result = apiClient.getSyncStatus()

        result.onSuccess { response ->
            _syncState.update {
                it.copy(
                    lastSyncTimestamp = Instant.parse(response.lastSyncTimestamp),
                    lastSyncVersion = response.syncVersion,
                    pendingChanges =
                        with(response.pendingChanges) {
                            locations + placeVisits + trips + photos
                        }
                )
            }
        }

        return result
    }

    /**
     * Mark sync as needed (e.g., when local changes are made).
     */
    suspend fun markSyncNeeded() {
        _syncState.update {
            it.copy(pendingChanges = it.pendingChanges + 1)
        }
    }

    /**
     * Clear sync error.
     */
    suspend fun clearError() {
        _syncState.update { it.copy(error = null) }
    }

    private suspend fun updateSyncingState(
        isSyncing: Boolean,
        error: String?
    ) {
        _syncState.update {
            it.copy(isSyncing = isSyncing, error = error)
        }

        if (!isSyncing && error == null) {
            syncStateRepository.updateSyncState(_syncState.value)
        }
    }

    private fun countLocalChanges(changes: LocalChanges): Int =
        changes.locations.size +
            changes.placeVisits.size +
            changes.trips.size +
            changes.photos.size +
            (if (changes.settings != null) 1 else 0)

    private fun countAccepted(accepted: AcceptedEntities): Int =
        accepted.locations.size +
            accepted.placeVisits.size +
            accepted.trips.size +
            accepted.photos.size

    private fun countRejected(rejected: RejectedEntities): Int =
        rejected.locations.size +
            rejected.placeVisits.size +
            rejected.trips.size +
            rejected.photos.size
}

/**
 * Sync state repository implementation using local storage.
 */
expect class SyncStateRepositoryImpl : SyncStateRepository {
    override suspend fun getSyncState(): SyncState

    override suspend fun updateSyncState(state: SyncState)
}
