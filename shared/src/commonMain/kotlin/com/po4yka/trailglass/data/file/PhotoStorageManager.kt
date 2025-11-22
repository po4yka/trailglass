package com.po4yka.trailglass.data.file

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Manages photo storage operations, coordinating between file storage and database.
 * This class ensures photos are properly saved to disk and database entries are created.
 */
@Inject
class PhotoStorageManager(
    private val photoFileManager: PhotoFileManager,
    private val photoRepository: PhotoRepository
) {
    private val logger = logger()

    /**
     * Save a photo to storage and database.
     *
     * @param photo Photo domain object with metadata
     * @param photoData Raw photo bytes to save
     * @param photosDirectory Directory to store photos
     * @return Result with the saved photo's file path
     */
    suspend fun savePhoto(
        photo: Photo,
        photoData: ByteArray,
        photosDirectory: String
    ): Result<String> {
        logger.info { "Saving photo ${photo.id} to storage" }

        return try {
            val saveResult =
                photoFileManager.savePhoto(
                    photoId = photo.id,
                    photoData = photoData,
                    photosDirectory = photosDirectory
                )

            if (saveResult.isFailure) {
                logger.error { "Failed to save photo file: ${saveResult.exceptionOrNull()?.message}" }
                return Result.failure(
                    saveResult.exceptionOrNull() ?: Exception("Failed to save photo file")
                )
            }

            val filePath = saveResult.getOrThrow()

            val updatedPhoto = photo.copy(uri = filePath)
            photoRepository.insertPhoto(updatedPhoto)

            logger.info { "Successfully saved photo ${photo.id} to $filePath" }
            Result.success(filePath)
        } catch (e: Exception) {
            logger.error(e) { "Failed to save photo ${photo.id}" }
            Result.failure(e)
        }
    }

    /**
     * Load a photo from storage.
     *
     * @param photoId Photo ID to load
     * @return Result with photo bytes, or null if photo not found
     */
    suspend fun loadPhoto(photoId: String): Result<ByteArray?> {
        logger.debug { "Loading photo $photoId from storage" }

        return try {
            val photo = photoRepository.getPhotoById(photoId)
            if (photo == null) {
                logger.warn { "Photo $photoId not found in database" }
                return Result.success(null)
            }

            val loadResult = photoFileManager.loadPhoto(photo.uri)
            if (loadResult.isFailure) {
                logger.error { "Failed to load photo file: ${loadResult.exceptionOrNull()?.message}" }
                return Result.failure(
                    loadResult.exceptionOrNull() ?: Exception("Failed to load photo file")
                )
            }

            Result.success(loadResult.getOrThrow())
        } catch (e: Exception) {
            logger.error(e) { "Failed to load photo $photoId" }
            Result.failure(e)
        }
    }

    /**
     * Delete a photo from both storage and database.
     *
     * @param photoId Photo ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deletePhoto(photoId: String): Result<Unit> {
        logger.info { "Deleting photo $photoId" }

        return try {
            val photo = photoRepository.getPhotoById(photoId)
            if (photo == null) {
                logger.warn { "Photo $photoId not found in database (already deleted?)" }
                return Result.success(Unit)
            }

            photoFileManager.deletePhoto(photo.uri).getOrThrow()

            photoRepository.deletePhoto(photoId)

            logger.info { "Successfully deleted photo $photoId" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete photo $photoId" }
            Result.failure(e)
        }
    }

    /**
     * Copy a photo from an external URI to app storage.
     *
     * @param sourceUri Source URI (e.g., content:// URI from photo picker)
     * @param photoId Target photo ID
     * @param photosDirectory Directory to store photos
     * @return Result with the destination file path
     */
    suspend fun copyPhotoFromUri(
        sourceUri: String,
        photoId: String,
        photosDirectory: String
    ): Result<String> {
        logger.info { "Copying photo from $sourceUri to app storage" }

        return try {
            val destinationPath = "$photosDirectory/$photoId.jpg"
            photoFileManager.copyPhoto(sourceUri, destinationPath).getOrThrow()

            logger.info { "Successfully copied photo to $destinationPath" }
            Result.success(destinationPath)
        } catch (e: Exception) {
            logger.error(e) { "Failed to copy photo from $sourceUri" }
            Result.failure(e)
        }
    }

    /**
     * Clean up orphaned photo files (photos not in database).
     *
     * @param photosDirectory Directory containing photos
     * @return Result with number of files deleted
     */
    suspend fun cleanupOrphanedPhotos(
        photosDirectory: String,
        userId: String
    ): Result<Int> {
        logger.info { "Cleaning up orphaned photos for user $userId" }

        return try {
            val allPhotos = photoRepository.getPhotosForUser(userId)
            val validPhotoIds = allPhotos.map { it.id }.toSet()

            val cleanupResult =
                photoFileManager.cleanupOrphanedPhotos(
                    photosDirectory = photosDirectory,
                    validPhotoIds = validPhotoIds
                )

            if (cleanupResult.isFailure) {
                logger.error { "Failed to cleanup orphaned photos: ${cleanupResult.exceptionOrNull()?.message}" }
                return Result.failure(
                    cleanupResult.exceptionOrNull() ?: Exception("Failed to cleanup orphaned photos")
                )
            }

            val deletedCount = cleanupResult.getOrThrow()
            logger.info { "Cleaned up $deletedCount orphaned photos" }
            Result.success(deletedCount)
        } catch (e: Exception) {
            logger.error(e) { "Failed to cleanup orphaned photos" }
            Result.failure(e)
        }
    }

    /**
     * Get storage statistics for photos.
     *
     * @param photosDirectory Directory containing photos
     * @return Result with total size in bytes
     */
    suspend fun getStorageStats(photosDirectory: String): Result<Long> {
        logger.debug { "Calculating photo storage stats" }

        return try {
            val totalSize = photoFileManager.calculateTotalPhotoSize(photosDirectory)
            if (totalSize.isFailure) {
                logger.error { "Failed to calculate storage stats: ${totalSize.exceptionOrNull()?.message}" }
                return Result.failure(
                    totalSize.exceptionOrNull() ?: Exception("Failed to calculate storage stats")
                )
            }

            Result.success(totalSize.getOrThrow())
        } catch (e: Exception) {
            logger.error(e) { "Failed to get storage stats" }
            Result.failure(e)
        }
    }

    /**
     * Check if a photo exists in storage.
     *
     * @param photoId Photo ID to check
     * @return true if photo exists, false otherwise
     */
    suspend fun photoExists(photoId: String): Boolean =
        try {
            val photo = photoRepository.getPhotoById(photoId)
            if (photo == null) {
                false
            } else {
                photoFileManager.photoExists(photo.uri)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error checking if photo $photoId exists" }
            false
        }
}
