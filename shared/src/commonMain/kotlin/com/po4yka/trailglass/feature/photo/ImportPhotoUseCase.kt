package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.photo.PhotoMetadataExtractor
import com.po4yka.trailglass.util.UuidGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Use case for importing a photo into TrailGlass.
 * Extracts metadata, stores photo record, and returns the created photo.
 */
@Inject
class ImportPhotoUseCase(
    private val photoRepository: PhotoRepository,
    private val metadataExtractor: PhotoMetadataExtractor
) {
    private val logger = logger()

    /**
     * Import a photo from a platform-specific URI.
     *
     * @param uri Platform-specific photo URI (content:// on Android, PHAsset ID on iOS)
     * @param userId User ID who owns this photo
     * @param timestamp Photo timestamp (will try to extract from EXIF if not provided)
     * @return Imported photo with metadata
     */
    suspend fun execute(
        uri: String,
        userId: String,
        timestamp: Instant? = null
    ): ImportResult =
        try {
            logger.info { "Importing photo from URI: $uri" }

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

            // Create photo record
            val photo =
                Photo(
                    id = photoId,
                    uri = uri,
                    timestamp = photoTimestamp,
                    latitude = latitude,
                    longitude = longitude,
                    width = null, // Would need platform-specific extraction
                    height = null,
                    sizeBytes = null,
                    mimeType = null, // Would need platform-specific extraction
                    userId = userId,
                    addedAt = Clock.System.now()
                )

            // Store photo
            photoRepository.insertPhoto(photo)

            logger.info { "Successfully imported photo ${photo.id}" }

            ImportResult.Success(photo, metadata)
        } catch (e: Exception) {
            logger.error(e) { "Failed to import photo from $uri" }
            ImportResult.Error(e.message ?: "Unknown error")
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
