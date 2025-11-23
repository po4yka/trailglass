package com.po4yka.trailglass.photo

import com.po4yka.trailglass.domain.model.Photo

/** Platform-agnostic interface for picking photos from the device. */
interface PhotoPicker {
    /**
     * Pick a single photo from the device.
     *
     * @return The selected photo, or null if cancelled
     */
    suspend fun pickPhoto(): Photo?

    /**
     * Pick multiple photos from the device.
     *
     * @param maxPhotos Maximum number of photos to select
     * @return List of selected photos (empty if cancelled)
     */
    suspend fun pickPhotos(maxPhotos: Int = 10): List<Photo>

    /** Check if photo picking is available (permissions granted). */
    suspend fun hasPermissions(): Boolean

    /**
     * Request photo library permissions.
     *
     * @return true if permissions were granted
     */
    suspend fun requestPermissions(): Boolean
}

/** Photo metadata extracted from the device. */
data class PhotoMetadata(
    val uri: String,
    val timestamp: kotlinx.datetime.Instant,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val width: Int? = null,
    val height: Int? = null,
    val sizeBytes: Long? = null,
    val mimeType: String? = null
)
