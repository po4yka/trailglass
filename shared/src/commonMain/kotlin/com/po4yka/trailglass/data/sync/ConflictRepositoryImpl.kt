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
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * SQLDelight-based persistent implementation of ConflictRepository.
 */
@Inject
class ConflictRepositoryImpl(
    private val database: Database
) : ConflictRepository {
    private val logger = logger()
    private val queries = database.syncConflictsQueries

    override suspend fun storeConflict(conflict: StoredConflict): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Storing conflict: ${conflict.conflictId}" }
            queries.storeConflict(
                conflict_id = conflict.conflictId,
                entity_type = conflict.entityType.name,
                entity_id = conflict.entityId,
                local_version = conflict.localVersion,
                remote_version = conflict.remoteVersion,
                local_data = conflict.localData,
                remote_data = conflict.remoteData,
                conflicted_fields = conflict.conflictedFields.joinToString(","),
                suggested_resolution = conflict.suggestedResolution,
                created_at = conflict.createdAt.toEpochMilliseconds(),
                status = conflict.status.name
            )
        }

    override suspend fun getConflict(conflictId: String): StoredConflict? =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting conflict: $conflictId" }
            queries
                .getConflict(conflictId)
                .asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .first()
                ?.let { row ->
                    StoredConflict(
                        conflictId = row.conflict_id,
                        entityType = EntityType.valueOf(row.entity_type),
                        entityId = row.entity_id,
                        localVersion = row.local_version,
                        remoteVersion = row.remote_version,
                        localData = row.local_data,
                        remoteData = row.remote_data,
                        conflictedFields = row.conflicted_fields.split(",").filter { it.isNotBlank() },
                        suggestedResolution = row.suggested_resolution,
                        createdAt = Instant.fromEpochMilliseconds(row.created_at),
                        status = ConflictStatus.valueOf(row.status)
                    )
                }
        }

    override suspend fun getPendingConflicts(): List<StoredConflict> =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting pending conflicts" }
            queries
                .getPendingConflicts()
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()
                .map { row ->
                    StoredConflict(
                        conflictId = row.conflict_id,
                        entityType = EntityType.valueOf(row.entity_type),
                        entityId = row.entity_id,
                        localVersion = row.local_version,
                        remoteVersion = row.remote_version,
                        localData = row.local_data,
                        remoteData = row.remote_data,
                        conflictedFields = row.conflicted_fields.split(",").filter { it.isNotBlank() },
                        suggestedResolution = row.suggested_resolution,
                        createdAt = Instant.fromEpochMilliseconds(row.created_at),
                        status = ConflictStatus.valueOf(row.status)
                    )
                }
        }

    override suspend fun getConflictCount(): Int =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting conflict count" }
            queries
                .getConflictCount()
                .executeAsOne()
                .toInt()
        }

    override suspend fun markAsResolved(conflictId: String): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Marking conflict as resolved: $conflictId" }
            queries.markAsResolved(conflictId)
        }

    override suspend fun markAsIgnored(conflictId: String): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Marking conflict as ignored: $conflictId" }
            queries.markAsIgnored(conflictId)
        }

    override suspend fun deleteConflict(conflictId: String): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Deleting conflict: $conflictId" }
            queries.deleteConflict(conflictId)
        }

    override suspend fun clearResolvedConflicts(): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Clearing resolved conflicts" }
            queries.clearResolvedConflicts()
        }
}
