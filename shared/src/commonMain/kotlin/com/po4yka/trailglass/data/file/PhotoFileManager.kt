package com.po4yka.trailglass.data.file

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

private val logger = KotlinLogging.logger {}

/**
 * Manages photo file operations using Kotlinx IO.
 * Handles storing, retrieving, and managing photo files across platforms.
 */
class PhotoFileManager(
    private val fileOperations: FileOperations
) {

    /**
     * Save photo data to storage.
     *
     * @param photoId Unique photo identifier
     * @param photoData Raw photo bytes
     * @param photosDirectory Directory to store photos
     * @return Path to the saved photo file
     */
    suspend fun savePhoto(
        photoId: String,
        photoData: ByteArray,
        photosDirectory: String
    ): Result<String> {
        return try {
            logger.info { "Saving photo: $photoId (${photoData.size} bytes)" }

            fileOperations.createDirectories(photosDirectory)

            val photoPath = "$photosDirectory/$photoId.jpg"
            fileOperations.writeFileBytes(photoPath, photoData)

            logger.info { "Successfully saved photo to: $photoPath" }
            Result.success(photoPath)
        } catch (e: Exception) {
            logger.error(e) { "Failed to save photo $photoId" }
            Result.failure(e)
        }
    }

    /**
     * Load photo data from storage.
     *
     * @param photoPath Path to the photo file
     * @return Photo data as ByteArray
     */
    suspend fun loadPhoto(photoPath: String): Result<ByteArray> {
        return try {
            logger.debug { "Loading photo from: $photoPath" }

            if (!fileOperations.fileExists(photoPath)) {
                logger.warn { "Photo not found: $photoPath" }
                return Result.failure(Exception("Photo not found: $photoPath"))
            }

            val photoData = fileOperations.readFileBytes(photoPath)
            logger.debug { "Successfully loaded photo: $photoPath (${photoData.size} bytes)" }
            Result.success(photoData)
        } catch (e: Exception) {
            logger.error(e) { "Failed to load photo from $photoPath" }
            Result.failure(e)
        }
    }

    /**
     * Delete a photo from storage.
     *
     * @param photoPath Path to the photo file
     * @return Result indicating success or failure
     */
    suspend fun deletePhoto(photoPath: String): Result<Unit> {
        return try {
            logger.info { "Deleting photo: $photoPath" }

            if (!fileOperations.fileExists(photoPath)) {
                logger.warn { "Photo not found (already deleted?): $photoPath" }
                return Result.success(Unit)
            }

            fileOperations.deleteFile(photoPath)
            logger.info { "Successfully deleted photo: $photoPath" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete photo $photoPath" }
            Result.failure(e)
        }
    }

    /**
     * Copy a photo to a new location.
     *
     * @param sourcePath Source photo path
     * @param destinationPath Destination path
     * @return Result indicating success or failure
     */
    suspend fun copyPhoto(
        sourcePath: String,
        destinationPath: String
    ): Result<Unit> {
        return try {
            logger.info { "Copying photo from $sourcePath to $destinationPath" }

            if (!fileOperations.fileExists(sourcePath)) {
                logger.error { "Source photo not found: $sourcePath" }
                return Result.failure(Exception("Source photo not found"))
            }

            val destinationDir = Path(destinationPath).parent?.toString()
            if (destinationDir != null) {
                fileOperations.createDirectories(destinationDir)
            }

            fileOperations.copyFile(sourcePath, destinationPath)
            logger.info { "Successfully copied photo to: $destinationPath" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to copy photo from $sourcePath to $destinationPath" }
            Result.failure(e)
        }
    }

    /**
     * Get photo file size in bytes.
     *
     * @param photoPath Path to the photo file
     * @return File size in bytes, or -1 if file doesn't exist
     */
    fun getPhotoSize(photoPath: String): Long {
        return fileOperations.getFileSize(photoPath)
    }

    /**
     * Check if a photo exists.
     *
     * @param photoPath Path to the photo file
     * @return true if photo exists, false otherwise
     */
    fun photoExists(photoPath: String): Boolean {
        return fileOperations.fileExists(photoPath)
    }

    /**
     * Clean up orphaned photo files (photos not referenced in the database).
     *
     * @param photosDirectory Directory containing photos
     * @param validPhotoIds Set of photo IDs that should be kept
     * @return Number of files deleted
     */
    suspend fun cleanupOrphanedPhotos(
        photosDirectory: String,
        validPhotoIds: Set<String>
    ): Result<Int> {
        return try {
            logger.info { "Cleaning up orphaned photos in: $photosDirectory" }

            val dirPath = Path(photosDirectory)
            if (!SystemFileSystem.exists(dirPath)) {
                logger.info { "Photos directory does not exist: $photosDirectory" }
                return Result.success(0)
            }

            var deletedCount = 0

            SystemFileSystem.list(dirPath).forEach { photoPath ->
                val fileName = photoPath.name
                if (fileName.endsWith(".jpg")) {
                    val photoId = fileName.removeSuffix(".jpg")
                    if (photoId !in validPhotoIds) {
                        logger.debug { "Deleting orphaned photo: $fileName" }
                        fileOperations.deleteFile(photoPath.toString())
                        deletedCount++
                    }
                }
            }

            logger.info { "Cleaned up $deletedCount orphaned photos" }
            Result.success(deletedCount)
        } catch (e: Exception) {
            logger.error(e) { "Failed to clean up orphaned photos" }
            Result.failure(e)
        }
    }

    /**
     * Calculate total size of all photos in a directory.
     *
     * @param photosDirectory Directory containing photos
     * @return Total size in bytes
     */
    suspend fun calculateTotalPhotoSize(photosDirectory: String): Result<Long> {
        return try {
            val dirPath = Path(photosDirectory)
            if (!SystemFileSystem.exists(dirPath)) {
                return Result.success(0L)
            }

            var totalSize = 0L
            SystemFileSystem.list(dirPath).forEach { photoPath ->
                if (photoPath.name.endsWith(".jpg")) {
                    val size = fileOperations.getFileSize(photoPath.toString())
                    if (size > 0) {
                        totalSize += size
                    }
                }
            }

            logger.debug { "Total photo size in $photosDirectory: $totalSize bytes" }
            Result.success(totalSize)
        } catch (e: Exception) {
            logger.error(e) { "Failed to calculate total photo size" }
            Result.failure(e)
        }
    }
}
