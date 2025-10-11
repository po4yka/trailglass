package com.po4yka.trailglass.photo

/**
 * Factory for creating platform-specific PhotoPicker instances.
 */
expect class PhotoPickerFactory {
    /**
     * Create a PhotoPicker instance for the current platform.
     *
     * @param userId User ID for the photo owner
     * @return Platform-specific PhotoPicker implementation
     */
    fun create(userId: String): PhotoPicker
}
