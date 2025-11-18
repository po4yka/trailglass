package com.po4yka.trailglass.domain.model

import kotlinx.datetime.LocalDate

/**
 * A group of photos by date with optional location information.
 */
data class PhotoGroup(
    /**
     * Date for this group of photos.
     */
    val date: LocalDate,

    /**
     * Photos in this group, sorted by timestamp.
     */
    val photos: List<PhotoWithMetadata>,

    /**
     * Optional location name for this group (e.g., "New York, NY").
     */
    val location: String? = null
) {
    /**
     * Number of photos in this group.
     */
    val photoCount: Int get() = photos.size
}

/**
 * Photo with its metadata and attachments.
 */
data class PhotoWithMetadata(
    /**
     * The photo.
     */
    val photo: Photo,

    /**
     * Optional metadata (EXIF data, etc.).
     */
    val metadata: PhotoMetadata? = null,

    /**
     * Attachments to place visits.
     */
    val attachments: List<PhotoAttachment> = emptyList(),

    /**
     * Optional cluster ID for spatially related photos.
     */
    val clusterId: String? = null
)

/**
 * Photo EXIF metadata.
 */
data class PhotoMetadata(
    /**
     * Camera make (e.g., "Canon", "Apple").
     */
    val cameraMake: String? = null,

    /**
     * Camera model (e.g., "Canon EOS R5", "iPhone 14 Pro").
     */
    val cameraModel: String? = null,

    /**
     * Lens information.
     */
    val lens: String? = null,

    /**
     * Focal length in mm.
     */
    val focalLength: Double? = null,

    /**
     * Aperture (f-number).
     */
    val aperture: Double? = null,

    /**
     * ISO sensitivity.
     */
    val iso: Int? = null,

    /**
     * Shutter speed (e.g., "1/250", "2s").
     */
    val shutterSpeed: String? = null,

    /**
     * GPS latitude from EXIF.
     */
    val exifLatitude: Double? = null,

    /**
     * GPS longitude from EXIF.
     */
    val exifLongitude: Double? = null,

    /**
     * GPS altitude in meters.
     */
    val exifAltitude: Double? = null,

    /**
     * Flash fired status.
     */
    val flashFired: Boolean? = null,

    /**
     * White balance mode.
     */
    val whiteBalance: String? = null,

    /**
     * Exposure program.
     */
    val exposureProgram: String? = null
)
