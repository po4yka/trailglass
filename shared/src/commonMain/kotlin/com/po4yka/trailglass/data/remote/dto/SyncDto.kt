package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/** Sync action type for delta synchronization. */
@Serializable
enum class SyncAction {
    CREATE,
    UPDATE,
    DELETE
}

/** Conflict resolution strategy. */
@Serializable
enum class ConflictResolution {
    KEEP_LOCAL,
    KEEP_REMOTE,
    MERGE,
    MANUAL
}

/** Entity type for sync operations. */
@Serializable
enum class EntityType {
    LOCATION,
    PLACE_VISIT,
    TRIP,
    PHOTO,
    SETTINGS
}

/** Conflict type enumeration. */
@Serializable
enum class ConflictType {
    CONCURRENT_MODIFICATION,
    DELETION_CONFLICT,
    VERSION_MISMATCH
}

/** Device sync information. */
@Serializable
data class DeviceSyncInfoDto(
    val deviceId: String,
    val deviceName: String,
    val lastSyncAt: String,
    val syncVersion: Long
)

/** Pending changes count. */
@Serializable
data class PendingChangesDto(
    val locations: Int,
    val placeVisits: Int,
    val trips: Int,
    val photos: Int
)

/** Sync status response. */
@Serializable
data class SyncStatusResponse(
    val userId: String,
    val lastSyncTimestamp: String,
    val syncVersion: Long,
    val deviceSyncInfo: List<DeviceSyncInfoDto>,
    val pendingChanges: PendingChangesDto
)

/** Local changes for delta sync. */
@Serializable
data class LocalChanges(
    val locations: List<LocationDto> = emptyList(),
    val placeVisits: List<PlaceVisitDto> = emptyList(),
    val trips: List<TripDto> = emptyList(),
    val photos: List<PhotoMetadataDto> = emptyList(),
    val settings: SettingsDto? = null
)

/** Remote changes from server. */
@Serializable
data class RemoteChanges(
    val locations: List<LocationDto> = emptyList(),
    val placeVisits: List<PlaceVisitDto> = emptyList(),
    val trips: List<TripDto> = emptyList(),
    val photos: List<PhotoMetadataDto> = emptyList(),
    val deletedIds: DeletedIds = DeletedIds()
)

/** Deleted entity IDs by type. */
@Serializable
data class DeletedIds(
    val locations: List<String> = emptyList(),
    val placeVisits: List<String> = emptyList(),
    val trips: List<String> = emptyList(),
    val photos: List<String> = emptyList()
)

/** Sync conflict information. */
@Serializable
data class SyncConflictDto(
    val entityType: EntityType,
    val entityId: String,
    val conflictType: ConflictType,
    val localVersion: Map<String, String>,
    val remoteVersion: Map<String, String>,
    val suggestedResolution: ConflictResolution
)

/** Rejected entity with reason. */
@Serializable
data class RejectedEntity(
    val id: String,
    val reason: String,
    val conflictId: String? = null
)

/** Rejected entities by type. */
@Serializable
data class RejectedEntities(
    val locations: List<RejectedEntity> = emptyList(),
    val placeVisits: List<RejectedEntity> = emptyList(),
    val trips: List<RejectedEntity> = emptyList(),
    val photos: List<RejectedEntity> = emptyList()
)

/** Accepted entity IDs by type. */
@Serializable
data class AcceptedEntities(
    val locations: List<String> = emptyList(),
    val placeVisits: List<String> = emptyList(),
    val trips: List<String> = emptyList(),
    val photos: List<String> = emptyList()
)

/** Delta sync request. */
@Serializable
data class DeltaSyncRequest(
    val deviceId: String,
    val lastSyncVersion: Long,
    val localChanges: LocalChanges
)

/** Delta sync response. */
@Serializable
data class DeltaSyncResponse(
    val syncVersion: Long,
    val syncTimestamp: String,
    val conflicts: List<SyncConflictDto>,
    val remoteChanges: RemoteChanges,
    val accepted: AcceptedEntities,
    val rejected: RejectedEntities
)

/** Conflict resolution request. */
@Serializable
data class ResolveConflictRequest(
    val conflictId: String,
    val resolution: ConflictResolution,
    val resolvedEntity: Map<String, String>
)

/** Accepted entity response. */
@Serializable
data class AcceptedEntityResponse(
    val id: String,
    val serverVersion: Long,
    val syncTimestamp: String
)

/** Conflict resolution response. */
@Serializable
data class ResolveConflictResponse(
    val success: Boolean,
    val syncVersion: Long,
    val acceptedEntity: AcceptedEntityResponse
)
