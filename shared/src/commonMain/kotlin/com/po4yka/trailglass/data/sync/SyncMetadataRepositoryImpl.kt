package com.po4yka.trailglass.data.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * In-memory implementation of SyncMetadataRepository.
 *
 * TODO: Replace with persistent storage (SQLDelight table) in production.
 * This implementation is sufficient for demonstrating the sync integration pattern.
 */
@Inject
class SyncMetadataRepositoryImpl : SyncMetadataRepository {

    private val metadata = mutableMapOf<String, SyncMetadata>()
    private val mutex = Mutex()

    override suspend fun upsertMetadata(metadata: SyncMetadata) = mutex.withLock {
        val key = getKey(metadata.entityId, metadata.entityType)
        this.metadata[key] = metadata
    }

    override suspend fun getMetadata(entityId: String, entityType: EntityType): SyncMetadata? = mutex.withLock {
        val key = getKey(entityId, entityType)
        metadata[key]
    }

    override suspend fun getPendingSync(entityType: EntityType, limit: Int): List<SyncMetadata> = mutex.withLock {
        metadata.values
            .filter { it.entityType == entityType && it.isPendingSync && !it.isPendingDelete }
            .take(limit)
    }

    override suspend fun getPendingDelete(entityType: EntityType): List<SyncMetadata> = mutex.withLock {
        metadata.values
            .filter { it.entityType == entityType && it.isPendingDelete }
    }

    override suspend fun markAsSynced(entityId: String, entityType: EntityType, serverVersion: Long) = mutex.withLock {
        val key = getKey(entityId, entityType)
        metadata[key]?.let { current ->
            metadata[key] = current.copy(
                serverVersion = serverVersion,
                lastSynced = Clock.System.now(),
                isPendingSync = false,
                syncAttempts = 0,
                lastSyncError = null
            )
        }
    }

    override suspend fun markSyncFailed(entityId: String, entityType: EntityType, error: String) = mutex.withLock {
        val key = getKey(entityId, entityType)
        metadata[key]?.let { current ->
            metadata[key] = current.copy(
                syncAttempts = current.syncAttempts + 1,
                lastSyncError = error
            )
        }
    }

    override suspend fun deletemetadata(entityId: String, entityType: EntityType) = mutex.withLock {
        val key = getKey(entityId, entityType)
        metadata.remove(key)
    }

    override suspend fun getAllPendingSync(): List<SyncMetadata> = mutex.withLock {
        metadata.values.filter { it.isPendingSync && !it.isPendingDelete }
    }

    override suspend fun getPendingSyncCount(): Int = mutex.withLock {
        metadata.values.count { it.isPendingSync && !it.isPendingDelete }
    }

    override suspend fun getLastSyncedMetadata(): SyncMetadata? = mutex.withLock {
        metadata.values
            .filter { it.lastSynced != null }
            .maxByOrNull { it.lastSynced!! }
    }

    private fun getKey(entityId: String, entityType: EntityType) = "${entityType.name}:$entityId"
}
