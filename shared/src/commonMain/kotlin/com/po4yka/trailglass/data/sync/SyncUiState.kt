package com.po4yka.trailglass.data.sync

import kotlinx.datetime.Instant

/**
 * UI-friendly representation of sync conflicts.
 */
data class ConflictUiModel(
    val conflictId: String,
    val entityType: EntityType,
    val entityId: String,
    val entityName: String, // User-friendly name
    val localVersion: Long,
    val remoteVersion: Long,
    val localModified: Instant,
    val remoteModified: Instant,
    val conflictDescription: String,
    val localPreview: String, // JSON or formatted preview
    val remotePreview: String
)

/**
 * Sync status with user-friendly information.
 */
data class SyncStatusUiModel(
    val isActive: Boolean,
    val progress: SyncProgress,
    val lastSyncTime: Instant?,
    val pendingCount: Int,
    val conflictCount: Int,
    val lastError: String?
)

/**
 * Resolution choice for a conflict.
 */
enum class ConflictResolutionChoice {
    KEEP_LOCAL,
    KEEP_REMOTE,
    MERGE,
    MANUAL
}
