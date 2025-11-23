package com.po4yka.trailglass.data.file

/**
 * Provides platform-specific photo storage directory paths. Implementations should return absolute paths to app-private
 * storage locations.
 */
interface PhotoDirectoryProvider {
    /**
     * Get the directory path for storing photos. This should be an app-private directory with read/write access.
     *
     * @return Absolute path to photos directory
     */
    fun getPhotosDirectory(): String

    /**
     * Get the directory path for storing photo thumbnails. This should be an app-private directory with read/write
     * access.
     *
     * @return Absolute path to thumbnails directory
     */
    fun getThumbnailsDirectory(): String

    /**
     * Get the directory path for temporary photo files. Used for processing photos before moving them to final storage.
     *
     * @return Absolute path to temporary photos directory
     */
    fun getTempPhotosDirectory(): String
}
