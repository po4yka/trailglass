package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/** EXIF data DTO. */
@Serializable
data class ExifDataDto(
    val cameraModel: String? = null,
    val focalLength: Double? = null,
    val aperture: Double? = null,
    val iso: Int? = null,
    val shutterSpeed: String? = null
)

/** Photo metadata DTO. */
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

/** Photo upload metadata (for multipart upload). */
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

/** Photo upload response. */
@Serializable
data class PhotoUploadResponse(
    val id: String,
    val url: String,
    val thumbnailUrl: String,
    val serverVersion: Long,
    val syncTimestamp: String
)

/** Photo attachment DTO. */
@Serializable
data class PhotoAttachmentDto(
    val id: String,
    val photoId: String,
    val placeVisitId: String,
    val attachedAt: String,
    val caption: String? = null,
    val syncAction: SyncAction? = null,
    val localVersion: Long? = null,
    val serverVersion: Long? = null,
    val deviceId: String? = null
)

/** Photo attachments response. */
@Serializable
data class PhotoAttachmentsResponse(
    val attachments: List<PhotoAttachmentDto>
)

/** Sync photo attachments request. */
@Serializable
data class SyncPhotoAttachmentsRequest(
    val attachments: List<PhotoAttachmentDto>
)

/** Sync photo attachments response. */
@Serializable
data class SyncPhotoAttachmentsResponse(
    val attachments: List<PhotoAttachmentDto>
)
