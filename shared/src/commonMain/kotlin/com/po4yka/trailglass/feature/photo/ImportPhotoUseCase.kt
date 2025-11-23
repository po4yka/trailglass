package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.file.PhotoDirectoryProvider
import com.po4yka.trailglass.data.file.PhotoStorageManager
import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.photo.PhotoMetadataExtractor
import com.po4yka.trailglass.util.UuidGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Use case for importing a photo into TrailGlass. Extracts metadata, copies photo to app storage, and stores photo
 * record.
 */
@Inject
class ImportPhotoUseCase(
    private val photoRepository: PhotoRepository,
    private val photoStorageManager: PhotoStorageManager,
    private val photoDirectoryProvider: PhotoDirectoryProvider,
    private val metadataExtractor: PhotoMetadataExtractor
) {
    private val logger = logger()

    /**
     * Import a photo from a platform-specific URI. This copies the photo to app storage and creates a database entry.
     *
     * @param uri Platform-specific photo URI (content:// on Android, PHAsset ID on iOS)
     * @param photoData Raw photo bytes (must be provided)
     * @param userId User ID who owns this photo
     * @param timestamp Photo timestamp (will try to extract from EXIF if not provided)
     * @return Imported photo with metadata
     */
    suspend fun execute(
        uri: String,
        photoData: ByteArray,
        userId: String,
        timestamp: Instant? = null
    ): ImportResult {
        return try {
            logger.info { "Importing photo from URI: $uri (${photoData.size} bytes)" }

            val photoId = UuidGenerator.randomUUID()

            // Extract metadata
            val metadata = metadataExtractor.extractMetadata(uri, photoId)
            logger.debug { "Extracted metadata: $metadata" }

            // Determine timestamp (EXIF > provided > current time)
            val photoTimestamp =
                metadata?.exifTimestampOriginal
                    ?: timestamp
                    ?: Clock.System.now()

            // Get location from EXIF if available
            val latitude = metadata?.exifLatitude
            val longitude = metadata?.exifLongitude

            // Get photos directory
            val photosDirectory = photoDirectoryProvider.getPhotosDirectory()

            // Create photo record (URI will be updated after save)
            val photo =
                Photo(
                    id = photoId,
                    uri = uri,
                    timestamp = photoTimestamp,
                    latitude = latitude,
                    longitude = longitude,
                    width = null,
                    height = null,
                    sizeBytes = photoData.size.toLong(),
                    mimeType = "image/jpeg",
                    userId = userId,
                    addedAt = Clock.System.now()
                )

            // Save photo to storage (this also inserts into database with updated URI)
            val saveResult =
                photoStorageManager.savePhoto(
                    photo = photo,
                    photoData = photoData,
                    photosDirectory = photosDirectory
                )

            if (saveResult.isFailure) {
                logger.error { "Failed to save photo: ${saveResult.exceptionOrNull()?.message}" }
                val errorMessage = saveResult.exceptionOrNull()?.message ?: "Failed to save photo"
                return ImportResult.Error(errorMessage)
            }

            logger.info { "Successfully imported photo ${photo.id}" }

            ImportResult.Success(photo, metadata)
        } catch (e: Exception) {
            logger.error(e) { "Failed to import photo from $uri" }
            ImportResult.Error(e.message ?: "Unknown error")
        }
    }

    sealed class ImportResult {
        data class Success(
            val photo: Photo,
            val metadata: com.po4yka.trailglass.domain.model.PhotoMetadata?
        ) : ImportResult()

        data class Error(
            val message: String
        ) : ImportResult()
    }
}
