package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.file.PhotoDirectoryProvider
import com.po4yka.trailglass.data.file.PhotoStorageManager
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for cleaning up orphaned photo files. Orphaned photos are files that exist in storage but have no
 * corresponding database entry.
 */
@Inject
class CleanupPhotosUseCase(
    private val photoStorageManager: PhotoStorageManager,
    private val photoDirectoryProvider: PhotoDirectoryProvider
) {
    private val logger = logger()

    /**
     * Clean up orphaned photos for a user.
     *
     * @param userId User ID to clean up photos for
     * @return Number of orphaned photos deleted
     */
    suspend fun execute(userId: String): Result<Int> {
        logger.info { "Cleaning up orphaned photos for user $userId" }

        return try {
            val photosDirectory = photoDirectoryProvider.getPhotosDirectory()
            val result =
                photoStorageManager.cleanupOrphanedPhotos(
                    photosDirectory = photosDirectory,
                    userId = userId
                )

            if (result.isFailure) {
                logger.error { "Failed to cleanup photos: ${result.exceptionOrNull()?.message}" }
                return Result.Error(
                    TrailGlassError.PhotoError.LoadFailed(
                        technicalMessage = result.exceptionOrNull()?.message ?: "Failed to cleanup photos",
                        cause = result.exceptionOrNull()
                    )
                )
            }

            val deletedCount = result.getOrThrow()
            logger.info { "Cleaned up $deletedCount orphaned photos" }
            Result.Success(deletedCount)
        } catch (e: Exception) {
            logger.error(e) { "Failed to cleanup orphaned photos" }
            Result.Error(
                TrailGlassError.PhotoError.LoadFailed(
                    technicalMessage = e.message ?: "Failed to cleanup orphaned photos",
                    cause = e
                )
            )
        }
    }
}
