package com.po4yka.trailglass.data.file

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of PhotoDirectoryProvider. Uses NSDocumentDirectory for photos and NSCachesDirectory for temporary
 * files.
 */
class IOSPhotoDirectoryProvider : PhotoDirectoryProvider {
    override fun getPhotosDirectory(): String {
        val documentsPath = getDocumentsDirectory()
        val photosPath = "$documentsPath/$PHOTOS_DIRECTORY_NAME"
        ensureDirectoryExists(photosPath)
        return photosPath
    }

    override fun getThumbnailsDirectory(): String {
        val documentsPath = getDocumentsDirectory()
        val thumbnailsPath = "$documentsPath/$THUMBNAILS_DIRECTORY_NAME"
        ensureDirectoryExists(thumbnailsPath)
        return thumbnailsPath
    }

    override fun getTempPhotosDirectory(): String {
        val cachesPath = getCachesDirectory()
        val tempPath = "$cachesPath/$TEMP_PHOTOS_DIRECTORY_NAME"
        ensureDirectoryExists(tempPath)
        return tempPath
    }

    private fun getDocumentsDirectory(): String {
        val paths =
            NSFileManager.defaultManager.URLsForDirectory(
                directory = NSDocumentDirectory,
                inDomains = NSUserDomainMask
            )
        return (paths.firstOrNull() as? NSURL)?.path ?: ""
    }

    private fun getCachesDirectory(): String {
        val paths =
            NSFileManager.defaultManager.URLsForDirectory(
                directory = NSCachesDirectory,
                inDomains = NSUserDomainMask
            )
        return (paths.firstOrNull() as? NSURL)?.path ?: ""
    }

    private fun ensureDirectoryExists(path: String) {
        val fileManager = NSFileManager.defaultManager
        val exists = fileManager.fileExistsAtPath(path)

        if (!exists) {
            fileManager.createDirectoryAtPath(
                path = path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
    }

    companion object {
        private const val PHOTOS_DIRECTORY_NAME = "photos"
        private const val THUMBNAILS_DIRECTORY_NAME = "thumbnails"
        private const val TEMP_PHOTOS_DIRECTORY_NAME = "temp_photos"
    }
}
