package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.remote.dto.SyncConflictDto
import kotlinx.datetime.Instant

/** Stored conflict requiring manual resolution. */
data class StoredConflict(
    val conflictId: String,
    val entityType: EntityType,
    val entityId: String,
    val localVersion: Long,
    val remoteVersion: Long,
    val localData: String, // JSON serialized
    val remoteData: String, // JSON serialized
    val conflictedFields: List<String>,
    val suggestedResolution: String,
    val createdAt: Instant,
    val status: ConflictStatus = ConflictStatus.PENDING
)

/** Status of a conflict. */
enum class ConflictStatus {
    PENDING,
    RESOLVED,
    IGNORED
}

/** Repository for managing conflicts that need manual resolution. */
interface ConflictRepository {
    suspend fun storeConflict(conflict: StoredConflict)

    suspend fun getConflict(conflictId: String): StoredConflict?

    suspend fun getPendingConflicts(): List<StoredConflict>

    suspend fun getConflictCount(): Int

    suspend fun markAsResolved(conflictId: String)

    suspend fun markAsIgnored(conflictId: String)

    suspend fun deleteConflict(conflictId: String)

    suspend fun clearResolvedConflicts()
}

/** Extension to convert SyncConflictDto to StoredConflict. */
fun SyncConflictDto.toStoredConflict(): StoredConflict {
    // Convert remote EntityType to sync EntityType
    val syncEntityType =
        when (entityType) {
            com.po4yka.trailglass.data.remote.dto.EntityType.LOCATION -> EntityType.LOCATION_SAMPLE
            com.po4yka.trailglass.data.remote.dto.EntityType.PLACE_VISIT -> EntityType.PLACE_VISIT
            com.po4yka.trailglass.data.remote.dto.EntityType.TRIP -> EntityType.TRIP
            com.po4yka.trailglass.data.remote.dto.EntityType.PHOTO -> EntityType.PHOTO
            com.po4yka.trailglass.data.remote.dto.EntityType.SETTINGS -> EntityType.SETTINGS
        }

    return StoredConflict(
        conflictId = "${entityType.name}_${entityId}_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
        entityType = syncEntityType,
        entityId = entityId,
        localVersion = localVersion["version"]?.toLongOrNull() ?: 0L,
        remoteVersion = remoteVersion["version"]?.toLongOrNull() ?: 0L,
        localData = localVersion.toString(),
        remoteData = remoteVersion.toString(),
        conflictedFields = emptyList(), // Not provided in DTO
        suggestedResolution = suggestedResolution.name,
        createdAt =
            kotlinx.datetime.Clock.System
                .now()
    )
}
