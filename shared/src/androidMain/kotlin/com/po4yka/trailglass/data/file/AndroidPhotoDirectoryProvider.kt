package com.po4yka.trailglass.data.file

import android.content.Context
import java.io.File

/** Android implementation of PhotoDirectoryProvider. Uses app-specific internal storage directories. */
class AndroidPhotoDirectoryProvider(
    private val context: Context
) : PhotoDirectoryProvider {
    override fun getPhotosDirectory(): String {
        val photosDir = File(context.filesDir, PHOTOS_DIRECTORY_NAME)
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        return photosDir.absolutePath
    }

    override fun getThumbnailsDirectory(): String {
        val thumbnailsDir = File(context.filesDir, THUMBNAILS_DIRECTORY_NAME)
        if (!thumbnailsDir.exists()) {
            thumbnailsDir.mkdirs()
        }
        return thumbnailsDir.absolutePath
    }

    override fun getTempPhotosDirectory(): String {
        val tempDir = File(context.cacheDir, TEMP_PHOTOS_DIRECTORY_NAME)
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return tempDir.absolutePath
    }

    companion object {
        private const val PHOTOS_DIRECTORY_NAME = "photos"
        private const val THUMBNAILS_DIRECTORY_NAME = "thumbnails"
        private const val TEMP_PHOTOS_DIRECTORY_NAME = "temp_photos"
    }
}
