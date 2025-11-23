package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/** User statistics DTO. */
@Serializable
data class UserStatisticsDto(
    val totalLocations: Long,
    val totalPlaceVisits: Int,
    val totalTrips: Int,
    val totalPhotos: Int,
    val countriesVisited: Int,
    val totalDistance: Double
)

/** User profile DTO. */
@Serializable
data class UserProfileDto(
    val userId: String,
    val email: String,
    val displayName: String,
    val profilePhotoUrl: String? = null,
    val createdAt: String,
    val statistics: UserStatisticsDto? = null
)

/** Update user profile request. */
@Serializable
data class UpdateUserProfileRequest(
    val displayName: String? = null,
    val profilePhoto: String? = null // Base64 encoded image
)

/** Update user profile response. */
@Serializable
data class UpdateUserProfileResponse(
    val userId: String,
    val profilePhotoUrl: String? = null,
    val updatedAt: String
)

/** Device info DTO (for listing devices). */
@Serializable
data class DeviceDto(
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String,
    val lastSyncAt: String,
    val registeredAt: String,
    val isActive: Boolean
)

/** User devices response. */
@Serializable
data class UserDevicesResponse(
    val devices: List<DeviceDto>
)

/** Data export request. */
@Serializable
data class DataExportRequest(
    val format: String, // JSON, CSV, KML, etc.
    val includePhotos: Boolean,
    val startDate: String? = null,
    val endDate: String? = null
)

/** Data export status. */
@Serializable
enum class ExportStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

/** Data export response. */
@Serializable
data class DataExportResponse(
    val exportId: String,
    val status: ExportStatus,
    val estimatedCompletionTime: String? = null
)

/** Export status response. */
@Serializable
data class ExportStatusResponse(
    val exportId: String,
    val status: ExportStatus,
    val downloadUrl: String? = null,
    val expiresAt: String? = null,
    val fileSize: Long? = null
)
