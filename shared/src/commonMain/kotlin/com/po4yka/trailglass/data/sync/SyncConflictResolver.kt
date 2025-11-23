package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.sync.SyncUiState.ConflictResolutionChoice
import com.po4yka.trailglass.data.sync.SyncUiState.ConflictUiModel
import com.po4yka.trailglass.logging.logger

/**
 * Handles conflict detection, storage, and resolution.
 *
 * Responsibilities:
 * - Handle conflicts returned from sync
 * - Automatically resolve conflicts when possible
 * - Store conflicts for manual resolution
 * - Provide UI-friendly conflict models
 * - Execute conflict resolution choices
 */
internal class SyncConflictResolver(
    private val conflictRepository: ConflictRepository,
    private val syncMetadataRepository: SyncMetadataRepository,
    private val syncCoordinator: SyncCoordinator,
    private val placeVisitRepository: PlaceVisitRepository,
    private val tripRepository: TripRepository
) {
    private val logger = logger()

    /**
     * Handle sync conflicts. Stores conflicts for manual resolution through UI.
     * Returns the number of conflicts automatically resolved.
     */
    suspend fun handleConflicts(conflicts: List<com.po4yka.trailglass.data.remote.dto.SyncConflictDto>): Int {
        var resolved = 0

        for (conflict in conflicts) {
            try {
                // Check if suggested resolution is automatic
                if (conflict.suggestedResolution == com.po4yka.trailglass.data.remote.dto.ConflictResolution.MERGE ||
                    conflict.suggestedResolution == com.po4yka.trailglass.data.remote.dto.ConflictResolution.KEEP_REMOTE
                ) {
                    // Apply automatic resolution
                    val resolution =
                        when (conflict.suggestedResolution) {
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
    suspend fun resolveConflictAutomatically(
        conflict: com.po4yka.trailglass.data.remote.dto.SyncConflictDto,
        choice: ConflictResolutionChoice
    ) {
        when (choice) {
            ConflictResolutionChoice.KEEP_REMOTE -> {
                // Remote wins - data will be applied in applyRemoteChanges
                // Just mark local as synced with remote version
                val entityType =
                    when (conflict.entityType) {
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
                val entityType =
                    when (conflict.entityType) {
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
     * Resolve a conflict with the given choice.
     */
    suspend fun resolveConflict(
        conflictId: String,
        choice: ConflictResolutionChoice
    ): Result<Unit> {
        return try {
            val conflict =
                conflictRepository.getConflict(conflictId)
                    ?: return Result.failure(Exception("Conflict not found: $conflictId"))

            logger.info { "Resolving conflict $conflictId with choice $choice" }

            when (choice) {
                ConflictResolutionChoice.KEEP_LOCAL -> {
                    // Local wins - mark entity for re-upload
                    val metadata =
                        syncMetadataRepository.getMetadata(
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
     * Convert StoredConflict to UI-friendly ConflictUiModel.
     */
    suspend fun convertToUiModel(conflict: StoredConflict): ConflictUiModel {
        // Get entity name based on type
        val entityName =
            when (conflict.entityType) {
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
}
