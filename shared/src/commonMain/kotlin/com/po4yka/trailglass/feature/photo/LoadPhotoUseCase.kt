package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.file.PhotoStorageManager
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for loading photo data from storage.
 */
@Inject
class LoadPhotoUseCase(
    private val photoStorageManager: PhotoStorageManager
) {
    private val logger = logger()

    /**
     * Load photo bytes from storage.
     *
     * @param photoId Photo ID to load
     * @return Photo bytes, or null if not found
     */
    suspend fun execute(photoId: String): Result<ByteArray?> {
        logger.debug { "Loading photo $photoId" }

        return try {
            val result = photoStorageManager.loadPhoto(photoId)
            if (result.isFailure) {
                logger.error { "Failed to load photo: ${result.exceptionOrNull()?.message}" }
                return Result.failure(
                    result.exceptionOrNull() ?: Exception("Failed to load photo")
                )
            }

            Result.success(result.getOrNull())
        } catch (e: Exception) {
            logger.error(e) { "Failed to load photo $photoId" }
            Result.failure(e)
        }
    }
}
