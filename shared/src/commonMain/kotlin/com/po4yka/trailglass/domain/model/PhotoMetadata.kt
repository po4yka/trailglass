package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant

/**
 * EXIF and extended metadata for a photo.
 */
data class PhotoMetadata(
    val photoId: String,

    // EXIF Location data
    val exifLatitude: Double? = null,
    val exifLongitude: Double? = null,
    val exifAltitude: Double? = null,

    // EXIF Camera data
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lens: String? = null,

    // EXIF Settings
    val focalLength: Double? = null, // mm
    val aperture: Double? = null, // f-number
    val iso: Int? = null,
    val shutterSpeed: String? = null,
    val flash: Boolean? = null,

    // EXIF Date/Time
    val exifTimestamp: Instant? = null,
    val exifTimestampOriginal: Instant? = null,

    // Image properties
    val orientation: Int? = null, // EXIF orientation (1-8)
    val colorSpace: String? = null,

    // Computed metadata
    val computedLocation: Location? = null,
    val locationSource: LocationSource = LocationSource.NONE
) {
    enum class LocationSource {
        NONE,
        EXIF,
        COMPUTED,
        MANUAL
    }
}

/**
 * Photo cluster grouping photos by location and time.
 */
data class PhotoCluster(
    val id: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val startTime: Instant,
    val endTime: Instant,
    val photoCount: Int,
    val thumbnailPhotoId: String, // Representative photo
    val associatedVisitId: String? = null,
    val clusteredAt: Instant
) {
    val duration: kotlin.time.Duration get() = endTime - startTime
}

/**
 * Photo with full metadata and associations.
 */
data class PhotoWithMetadata(
    val photo: Photo,
    val metadata: PhotoMetadata?,
    val attachments: List<PhotoAttachment>,
    val clusterId: String? = null
)

/**
 * Photo grouped by date for gallery display.
 */
data class PhotoGroup(
    val date: kotlinx.datetime.LocalDate,
    val photos: List<PhotoWithMetadata>,
    val location: String? = null // City or location name
) {
    val photoCount: Int get() = photos.size
}
