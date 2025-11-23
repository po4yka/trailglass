package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.logging.logger

/** Interface for resolving sync conflicts. */
interface ConflictResolver<T : SyncableEntity> {
    /**
     * Resolve a conflict between local and remote entities.
     *
     * @param conflict The conflict to resolve
     * @param strategy The resolution strategy to use
     * @return The resolved entity
     */
    suspend fun resolve(
        conflict: SyncConflict<T>,
        strategy: ResolutionStrategy
    ): T
}

/** Default conflict resolver implementation. */
class DefaultConflictResolver<T : SyncableEntity> : ConflictResolver<T> {
    private val logger = logger()

    override suspend fun resolve(
        conflict: SyncConflict<T>,
        strategy: ResolutionStrategy
    ): T {
        logger.debug {
            "Resolving conflict for ${conflict.entityType}:${conflict.entityId} " +
                "using strategy $strategy"
        }

        return when (strategy) {
            ResolutionStrategy.KEEP_LOCAL -> {
                logger.info { "Keeping local version for ${conflict.entityId}" }
                conflict.localEntity
            }

            ResolutionStrategy.KEEP_REMOTE -> {
                logger.info { "Keeping remote version for ${conflict.entityId}" }
                conflict.remoteEntity
            }

            ResolutionStrategy.MERGE -> {
                logger.info { "Attempting merge for ${conflict.entityId}" }
                attemptMerge(conflict)
            }

            ResolutionStrategy.MANUAL -> {
                throw ConflictRequiresManualResolutionException(conflict)
            }
        }
    }

    /**
     * Attempt to automatically merge conflicting entities. Default implementation uses last-write-wins based on
     * lastModified timestamp.
     */
    private fun attemptMerge(conflict: SyncConflict<T>): T {
        val local = conflict.localEntity
        val remote = conflict.remoteEntity

        // Use last-write-wins strategy based on lastModified timestamp
        return if (local.lastModified > remote.lastModified) {
            logger.debug {
                "Merge: local is newer (${local.lastModified} > ${remote.lastModified})"
            }
            local
        } else {
            logger.debug {
                "Merge: remote is newer (${remote.lastModified} >= ${local.lastModified})"
            }
            remote
        }
    }
}

/** Exception thrown when a conflict requires manual resolution. */
class ConflictRequiresManualResolutionException(
    val conflict: SyncConflict<*>
) : Exception("Conflict for ${conflict.entityType}:${conflict.entityId} requires manual resolution")

/** Conflict detection helper. */
object ConflictDetector {
    /** Detect if there's a conflict between local and remote entities. */
    fun <T : SyncableEntity> detectConflict(
        local: T,
        remote: T,
        lastSyncVersion: Long
    ): ConflictType? {
        // If both versions are the same, no conflict
        if (local.serverVersion == remote.serverVersion) {
            return null
        }

        // If local was deleted but remote was modified
        if (local.isDeleted &&
            remote.serverVersion != null &&
            remote.serverVersion!! > lastSyncVersion
        ) {
            return ConflictType.MODIFY_DELETE_CONFLICT
        }

        // If remote was deleted but local was modified
        if (remote.isDeleted &&
            local.serverVersion != null &&
            local.serverVersion!! > lastSyncVersion
        ) {
            return ConflictType.DELETE_MODIFY_CONFLICT
        }

        // If both were modified (concurrent modification)
        if (local.serverVersion != null &&
            remote.serverVersion != null &&
            local.serverVersion != remote.serverVersion &&
            local.lastModified != remote.lastModified
        ) {
            return ConflictType.CONCURRENT_MODIFICATION
        }

        // Version mismatch
        if (local.serverVersion != remote.serverVersion) {
            return ConflictType.VERSION_MISMATCH
        }

        return null
    }

    /** Suggest a resolution strategy based on conflict type. */
    fun suggestResolutionStrategy(conflictType: ConflictType): ResolutionStrategy =
        when (conflictType) {
            ConflictType.CONCURRENT_MODIFICATION -> ResolutionStrategy.MERGE
            ConflictType.DELETE_MODIFY_CONFLICT -> ResolutionStrategy.KEEP_LOCAL
            ConflictType.MODIFY_DELETE_CONFLICT -> ResolutionStrategy.KEEP_REMOTE
            ConflictType.VERSION_MISMATCH -> ResolutionStrategy.KEEP_REMOTE
        }
}
