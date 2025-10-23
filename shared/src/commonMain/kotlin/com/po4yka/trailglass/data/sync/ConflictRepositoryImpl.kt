package com.po4yka.trailglass.data.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject

/**
 * In-memory implementation of ConflictRepository.
 *
 * TODO: Replace with persistent storage (SQLDelight table) in production.
 * This implementation is sufficient for demonstrating the conflict resolution pattern.
 */
@Inject
class ConflictRepositoryImpl : ConflictRepository {

    private val conflicts = mutableMapOf<String, StoredConflict>()
    private val mutex = Mutex()

    override suspend fun storeConflict(conflict: StoredConflict) = mutex.withLock {
        conflicts[conflict.conflictId] = conflict
    }

    override suspend fun getConflict(conflictId: String): StoredConflict? = mutex.withLock {
        conflicts[conflictId]
    }

    override suspend fun getPendingConflicts(): List<StoredConflict> = mutex.withLock {
        conflicts.values.filter { it.status == ConflictStatus.PENDING }
    }

    override suspend fun getConflictCount(): Int = mutex.withLock {
        conflicts.values.count { it.status == ConflictStatus.PENDING }
    }

    override suspend fun markAsResolved(conflictId: String) = mutex.withLock {
        conflicts[conflictId]?.let { current ->
            conflicts[conflictId] = current.copy(status = ConflictStatus.RESOLVED)
        }
    }

    override suspend fun markAsIgnored(conflictId: String) = mutex.withLock {
        conflicts[conflictId]?.let { current ->
            conflicts[conflictId] = current.copy(status = ConflictStatus.IGNORED)
        }
    }

    override suspend fun deleteConflict(conflictId: String) = mutex.withLock {
        conflicts.remove(conflictId)
    }

    override suspend fun clearResolvedConflicts() = mutex.withLock {
        val toRemove = conflicts.values
            .filter { it.status == ConflictStatus.RESOLVED }
            .map { it.conflictId }

        toRemove.forEach { conflicts.remove(it) }
    }
}
