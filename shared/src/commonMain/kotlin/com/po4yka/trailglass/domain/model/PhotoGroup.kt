package com.po4yka.trailglass.domain.model

import kotlinx.datetime.LocalDate

/** A group of photos by date with optional location information. */
data class PhotoGroup(
    /** Date for this group of photos. */
    val date: LocalDate,
    /** Photos in this group, sorted by timestamp. */
    val photos: List<PhotoWithMetadata>,
    /** Optional location name for this group (e.g., "New York, NY"). */
    val location: String? = null
) {
    /** Number of photos in this group. */
    val photoCount: Int get() = photos.size
}

/** Photo with its metadata and attachments. */
data class PhotoWithMetadata(
    /** The photo. */
    val photo: Photo,
    /** Optional metadata (EXIF data, etc.). */
    val metadata: PhotoMetadata? = null,
    /** Attachments to place visits. */
    val attachments: List<PhotoAttachment> = emptyList(),
    /** Optional cluster ID for spatially related photos. */
    val clusterId: String? = null
)
