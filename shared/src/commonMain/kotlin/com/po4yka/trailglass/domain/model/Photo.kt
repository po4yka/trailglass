package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant

/**
 * A photo from the user's device.
 */
data class Photo(
    /**
     * Unique identifier for the photo.
     */
    val id: String,
    /**
     * Platform-specific URI/path to the photo.
     * Android: content:// URI
     * iOS: PHAsset localIdentifier
     */
    val uri: String,
    /**
     * When the photo was taken.
     */
    val timestamp: Instant,
    /**
     * Location where photo was taken (if available).
     */
    val latitude: Double? = null,
    val longitude: Double? = null,
    /**
     * Photo dimensions.
     */
    val width: Int? = null,
    val height: Int? = null,
    /**
     * File size in bytes.
     */
    val sizeBytes: Long? = null,
    /**
     * MIME type (e.g., "image/jpeg").
     */
    val mimeType: String? = null,
    /**
     * User ID who owns this photo.
     */
    val userId: String,
    /**
     * When this photo was added to TrailGlass.
     */
    val addedAt: Instant
)

/**
 * Attachment linking a photo to a place visit.
 */
data class PhotoAttachment(
    /**
     * Unique identifier for the attachment.
     */
    val id: String,
    /**
     * Photo ID.
     */
    val photoId: String,
    /**
     * Place visit ID this photo is attached to.
     */
    val placeVisitId: String,
    /**
     * When the attachment was created.
     */
    val attachedAt: Instant,
    /**
     * Optional caption/note for this attachment.
     */
    val caption: String? = null
)

/**
 * Photo with its attachments loaded.
 */
data class PhotoWithAttachments(
    val photo: Photo,
    val attachments: List<PhotoAttachment>
)
