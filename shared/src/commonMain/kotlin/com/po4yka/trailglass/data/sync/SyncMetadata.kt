package com.po4yka.trailglass.data.sync

import kotlinx.datetime.Instant

/**
 * Metadata tracking sync state for any entity.
 * Stored separately from the actual entity data to avoid schema changes.
 */
data class SyncMetadata(
    val entityId: String,
    val entityType: EntityType,
    val localVersion: Long = 1,
    val serverVersion: Long? = null,
    val lastModified: Instant,
    val lastSynced: Instant? = null,
    val isPendingSync: Boolean = true,
    val isPendingDelete: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncError: String? = null,
    val deviceId: String
)

/**
 * Entity types that can be synced.
 */
enum class EntityType {
    LOCATION_SAMPLE,
    PLACE_VISIT,
    TRIP,
    PHOTO,
    ROUTE_SEGMENT,
    SETTINGS
}

/**
 * Repository interface for managing sync metadata.
 */
interface SyncMetadataRepository {
    suspend fun upsertMetadata(metadata: SyncMetadata)

    suspend fun getMetadata(
        entityId: String,
        entityType: EntityType
    ): SyncMetadata?

    suspend fun getPendingSync(
        entityType: EntityType,
        limit: Int = 100
    ): List<SyncMetadata>

    suspend fun getPendingDelete(entityType: EntityType): List<SyncMetadata>

    suspend fun markAsSynced(
        entityId: String,
        entityType: EntityType,
        serverVersion: Long
    )

    suspend fun markSyncFailed(
        entityId: String,
        entityType: EntityType,
        error: String
    )

    suspend fun deleteMetadata(
        entityId: String,
        entityType: EntityType
    )

    suspend fun getAllPendingSync(): List<SyncMetadata>

    suspend fun getPendingSyncCount(): Int

    suspend fun getLastSyncedMetadata(): SyncMetadata?
}
