package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.data.remote.dto.SyncConflictDto
import kotlinx.datetime.Instant

/**
 * Stored conflict requiring manual resolution.
 */
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

/**
 * Status of a conflict.
 */
enum class ConflictStatus {
    PENDING,
    RESOLVED,
    IGNORED
}

/**
 * Repository for managing conflicts that need manual resolution.
 */
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

/**
 * Extension to convert SyncConflictDto to StoredConflict.
 */
fun SyncConflictDto.toStoredConflict(): StoredConflict {
    return StoredConflict(
        conflictId = conflictId,
        entityType = when (entityType) {
            "PLACE_VISIT" -> EntityType.PLACE_VISIT
            "TRIP" -> EntityType.TRIP
            "PHOTO" -> EntityType.PHOTO
            "LOCATION_SAMPLE" -> EntityType.LOCATION_SAMPLE
            "ROUTE_SEGMENT" -> EntityType.ROUTE_SEGMENT
            "SETTINGS" -> EntityType.SETTINGS
            else -> EntityType.PLACE_VISIT // Default fallback
        },
        entityId = entityId,
        localVersion = localVersion,
        remoteVersion = remoteVersion,
        localData = localData.toString(), // Assuming localData is a map or JSON-serializable
        remoteData = remoteData.toString(),
        conflictedFields = conflictedFields,
        suggestedResolution = suggestedResolution,
        createdAt = kotlinx.datetime.Clock.System.now()
    )
}
