package com.po4yka.trailglass.data.sync

import kotlinx.datetime.Instant

/** Sync state for tracking synchronization status. */
data class SyncState(
    val lastSyncTimestamp: Instant? = null,
    val lastSyncVersion: Long = 0,
    val isSyncing: Boolean = false,
    val pendingChanges: Int = 0,
    val error: String? = null
)

/** Syncable entity interface for entities that can be synced. */
interface SyncableEntity {
    val id: String
    val localVersion: Long
    val serverVersion: Long?
    val lastModified: Instant
    val deviceId: String
    val isDeleted: Boolean
    val isPendingSync: Boolean
}

/** Sync operation type. */
enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE,
    NO_CHANGE
}

/** Conflict resolution strategy. */
enum class ResolutionStrategy {
    /** Keep the local version and reject remote changes. */
    KEEP_LOCAL,

    /** Accept the remote version and discard local changes. */
    KEEP_REMOTE,

    /** Attempt to merge both versions (if possible). */
    MERGE,

    /** Let the user manually resolve the conflict. */
    MANUAL
}

/** Sync conflict information. */
data class SyncConflict<T : SyncableEntity>(
    val entityId: String,
    val entityType: String,
    val localEntity: T,
    val remoteEntity: T,
    val conflictType: ConflictType,
    val detectedAt: Instant,
    val suggestedResolution: ResolutionStrategy = ResolutionStrategy.MANUAL
)

/** Type of conflict detected. */
enum class ConflictType {
    /** Both local and remote versions were modified since last sync. */
    CONCURRENT_MODIFICATION,

    /** Entity was deleted remotely but modified locally. */
    DELETE_MODIFY_CONFLICT,

    /** Entity was deleted locally but modified remotely. */
    MODIFY_DELETE_CONFLICT,

    /** Server version doesn't match expected version. */
    VERSION_MISMATCH
}

/** Sync result for a single entity. */
sealed class SyncResult<T : SyncableEntity> {
    data class Success<T : SyncableEntity>(
        val entity: T,
        val operation: SyncOperation
    ) : SyncResult<T>()

    data class Conflict<T : SyncableEntity>(
        val conflict: SyncConflict<T>
    ) : SyncResult<T>()

    data class Failure<T : SyncableEntity>(
        val entityId: String,
        val error: String
    ) : SyncResult<T>()
}

/** Batch sync result. */
data class BatchSyncResult<T : SyncableEntity>(
    val successful: List<SyncResult.Success<T>>,
    val conflicts: List<SyncConflict<T>>,
    val failures: List<SyncResult.Failure<T>>,
    val syncVersion: Long,
    val syncTimestamp: Instant
) {
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
    val hasFailures: Boolean get() = failures.isNotEmpty()
    val isFullySuccessful: Boolean get() = !hasConflicts && !hasFailures
}

/** Sync statistics. */
data class SyncStatistics(
    val totalEntities: Int,
    val created: Int,
    val updated: Int,
    val deleted: Int,
    val conflicts: Int,
    val failures: Int,
    val durationMs: Long
)
