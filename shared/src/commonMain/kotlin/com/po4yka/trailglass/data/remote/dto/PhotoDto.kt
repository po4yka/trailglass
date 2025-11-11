package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * EXIF data DTO.
 */
@Serializable
data class ExifDataDto(
    val cameraModel: String? = null,
    val focalLength: Double? = null,
    val aperture: Double? = null,
    val iso: Int? = null,
    val shutterSpeed: String? = null
)

/**
 * Photo metadata DTO.
 */
@Serializable
data class PhotoMetadataDto(
    val id: String,
    val timestamp: String,
    val location: CoordinateDto? = null,
    val placeVisitId: String? = null,
    val tripId: String? = null,
    val caption: String? = null,
    val url: String? = null,
    val thumbnailUrl: String? = null,
    val exifData: ExifDataDto? = null,
    val syncAction: SyncAction? = null,
    val localVersion: Long? = null,
    val serverVersion: Long? = null,
    val lastModified: String? = null,
    val deviceId: String? = null
)

/**
 * Photo upload metadata (for multipart upload).
 */
@Serializable
data class PhotoUploadMetadata(
    val id: String,
    val timestamp: String,
    val location: CoordinateDto? = null,
    val placeVisitId: String? = null,
    val tripId: String? = null,
    val caption: String? = null,
    val exifData: ExifDataDto? = null
)

/**
 * Photo upload response.
 */
@Serializable
data class PhotoUploadResponse(
    val id: String,
    val url: String,
    val thumbnailUrl: String,
    val serverVersion: Long,
    val syncTimestamp: String
)
