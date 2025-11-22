package com.po4yka.trailglass.data.sync

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * SQLDelight-based persistent implementation of SyncMetadataRepository.
 */
@Inject
class SyncMetadataRepositoryImpl(
    private val database: Database
) : SyncMetadataRepository {
    private val logger = logger()
    private val queries = database.syncMetadataQueries

    override suspend fun upsertMetadata(metadata: SyncMetadata): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Upserting sync metadata: ${metadata.entityType}:${metadata.entityId}" }
            queries.upsertMetadata(
                entity_id = metadata.entityId,
                entity_type = metadata.entityType.name,
                local_version = metadata.localVersion,
                server_version = metadata.serverVersion,
                last_modified = metadata.lastModified.toEpochMilliseconds(),
                last_synced = metadata.lastSynced?.toEpochMilliseconds(),
                is_pending_sync = if (metadata.isPendingSync) 1L else 0L,
                is_pending_delete = if (metadata.isPendingDelete) 1L else 0L,
                sync_attempts = metadata.syncAttempts.toLong(),
                last_sync_error = metadata.lastSyncError,
                device_id = metadata.deviceId
            )
        }

    override suspend fun getMetadata(
        entityId: String,
        entityType: EntityType
    ): SyncMetadata? =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting metadata for: $entityType:$entityId" }
            queries
                .getMetadata(entityId, entityType.name)
                .asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .first()
                ?.let { row ->
                    SyncMetadata(
                        entityId = row.entity_id,
                        entityType = EntityType.valueOf(row.entity_type),
                        localVersion = row.local_version,
                        serverVersion = row.server_version,
                        lastModified = Instant.fromEpochMilliseconds(row.last_modified),
                        lastSynced = row.last_synced?.let { Instant.fromEpochMilliseconds(it) },
                        isPendingSync = row.is_pending_sync == 1L,
                        isPendingDelete = row.is_pending_delete == 1L,
                        syncAttempts = row.sync_attempts.toInt(),
                        lastSyncError = row.last_sync_error,
                        deviceId = row.device_id
                    )
                }
        }

    override suspend fun getPendingSync(
        entityType: EntityType,
        limit: Int
    ): List<SyncMetadata> =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting pending sync for: $entityType (limit: $limit)" }
            queries
                .getPendingSync(entityType.name, limit.toLong())
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()
                .map { row ->
                    SyncMetadata(
                        entityId = row.entity_id,
                        entityType = EntityType.valueOf(row.entity_type),
                        localVersion = row.local_version,
                        serverVersion = row.server_version,
                        lastModified = Instant.fromEpochMilliseconds(row.last_modified),
                        lastSynced = row.last_synced?.let { Instant.fromEpochMilliseconds(it) },
                        isPendingSync = row.is_pending_sync == 1L,
                        isPendingDelete = row.is_pending_delete == 1L,
                        syncAttempts = row.sync_attempts.toInt(),
                        lastSyncError = row.last_sync_error,
                        deviceId = row.device_id
                    )
                }
        }

    override suspend fun getPendingDelete(entityType: EntityType): List<SyncMetadata> =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting pending delete for: $entityType" }
            queries
                .getPendingDelete(entityType.name)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()
                .map { row ->
                    SyncMetadata(
                        entityId = row.entity_id,
                        entityType = EntityType.valueOf(row.entity_type),
                        localVersion = row.local_version,
                        serverVersion = row.server_version,
                        lastModified = Instant.fromEpochMilliseconds(row.last_modified),
                        lastSynced = row.last_synced?.let { Instant.fromEpochMilliseconds(it) },
                        isPendingSync = row.is_pending_sync == 1L,
                        isPendingDelete = row.is_pending_delete == 1L,
                        syncAttempts = row.sync_attempts.toInt(),
                        lastSyncError = row.last_sync_error,
                        deviceId = row.device_id
                    )
                }
        }

    override suspend fun markAsSynced(
        entityId: String,
        entityType: EntityType,
        serverVersion: Long
    ): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Marking as synced: $entityType:$entityId (version: $serverVersion)" }
            queries.markAsSynced(
                server_version = serverVersion,
                last_synced = Clock.System.now().toEpochMilliseconds(),
                entity_id = entityId,
                entity_type = entityType.name
            )
        }

    override suspend fun markSyncFailed(
        entityId: String,
        entityType: EntityType,
        error: String
    ): Unit =
        withContext(Dispatchers.IO) {
            logger.warn { "Marking sync failed: $entityType:$entityId - $error" }
            queries.markSyncFailed(
                last_sync_error = error,
                entity_id = entityId,
                entity_type = entityType.name
            )
        }

    override suspend fun deleteMetadata(
        entityId: String,
        entityType: EntityType
    ): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Deleting metadata: $entityType:$entityId" }
            queries.deleteMetadata(entityId, entityType.name)
        }

    override suspend fun getAllPendingSync(): List<SyncMetadata> =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting all pending sync" }
            queries
                .getAllPendingSync()
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()
                .map { row ->
                    SyncMetadata(
                        entityId = row.entity_id,
                        entityType = EntityType.valueOf(row.entity_type),
                        localVersion = row.local_version,
                        serverVersion = row.server_version,
                        lastModified = Instant.fromEpochMilliseconds(row.last_modified),
                        lastSynced = row.last_synced?.let { Instant.fromEpochMilliseconds(it) },
                        isPendingSync = row.is_pending_sync == 1L,
                        isPendingDelete = row.is_pending_delete == 1L,
                        syncAttempts = row.sync_attempts.toInt(),
                        lastSyncError = row.last_sync_error,
                        deviceId = row.device_id
                    )
                }
        }

    override suspend fun getPendingSyncCount(): Int =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting pending sync count" }
            queries
                .getPendingSyncCount()
                .executeAsOne()
                .toInt()
        }

    override suspend fun getLastSyncedMetadata(): SyncMetadata? =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting last synced metadata" }
            queries
                .getLastSyncedMetadata()
                .asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .first()
                ?.let { row ->
                    SyncMetadata(
                        entityId = row.entity_id,
                        entityType = EntityType.valueOf(row.entity_type),
                        localVersion = row.local_version,
                        serverVersion = row.server_version,
                        lastModified = Instant.fromEpochMilliseconds(row.last_modified),
                        lastSynced = row.last_synced?.let { Instant.fromEpochMilliseconds(it) },
                        isPendingSync = row.is_pending_sync == 1L,
                        isPendingDelete = row.is_pending_delete == 1L,
                        syncAttempts = row.sync_attempts.toInt(),
                        lastSyncError = row.last_sync_error,
                        deviceId = row.device_id
                    )
                }
        }
}
