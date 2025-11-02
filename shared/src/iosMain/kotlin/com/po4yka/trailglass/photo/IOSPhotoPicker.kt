package com.po4yka.trailglass.photo

import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.Photos.*
import java.util.UUID

/**
 * iOS implementation of PhotoPicker using PHPicker and Photos framework.
 * Note: This class handles metadata extraction. UI-based photo selection
 * must be handled by the iOS app layer using PHPickerViewController.
 */
class IOSPhotoPicker(
    private val userId: String
) : PhotoPicker {

    private val logger = logger()

    override suspend fun pickPhoto(): Photo? {
        logger.warn { "pickPhoto() must be called from iOS UI layer" }
        return null
    }

    override suspend fun pickPhotos(maxPhotos: Int): List<Photo> {
        logger.warn { "pickPhotos() must be called from iOS UI layer" }
        return emptyList()
    }

    override suspend fun hasPermissions(): Boolean {
        val authStatus = PHPhotoLibrary.authorizationStatus()
        return authStatus == PHAuthorizationStatusAuthorized ||
               authStatus == PHAuthorizationStatusLimited
    }

    override suspend fun requestPermissions(): Boolean {
        // Note: This needs to be called from the main thread
        logger.info { "Requesting photo library permissions" }

        return withContext(Dispatchers.Main) {
            val authStatus = PHPhotoLibrary.authorizationStatus()

            when (authStatus) {
                PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> {
                    true
                }
                else -> {
                    // Request permission - result comes asynchronously
                    PHPhotoLibrary.requestAuthorization { status ->
                        logger.info { "Photo library authorization: $status" }
                    }
                    false
                }
            }
        }
    }

    /**
     * Extract photo metadata from a PHAsset.
     * This is called after the user has selected photos via PHPickerViewController.
     */
    suspend fun extractPhotoFromAsset(asset: PHAsset): Photo? = withContext(Dispatchers.IO) {
        logger.debug { "Extracting photo metadata from PHAsset: ${asset.localIdentifier}" }

        try {
            val metadata = extractMetadataFromAsset(asset)

            Photo(
                id = "photo_${UUID.randomUUID()}",
                uri = asset.localIdentifier,
                timestamp = metadata.timestamp,
                latitude = metadata.latitude,
                longitude = metadata.longitude,
                width = metadata.width,
                height = metadata.height,
                sizeBytes = null, // Not easily available from PHAsset
                mimeType = metadata.mimeType,
                userId = userId,
                addedAt = Clock.System.now()
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to extract photo from PHAsset" }
            null
        }
    }

    /**
     * Extract multiple photos from PHAssets.
     */
    suspend fun extractPhotosFromAssets(assets: List<PHAsset>): List<Photo> = withContext(Dispatchers.IO) {
        logger.debug { "Extracting ${assets.size} photos from PHAssets" }

        assets.mapNotNull { asset ->
            extractPhotoFromAsset(asset)
        }
    }

    /**
     * Extract metadata from a PHAsset.
     */
    private fun extractMetadataFromAsset(asset: PHAsset): PhotoMetadata {
        // Extract timestamp
        val creationDate = asset.creationDate as? NSDate
        val timestamp = if (creationDate != null) {
            Instant.fromEpochMilliseconds((creationDate.timeIntervalSince1970 * 1000).toLong())
        } else {
            Clock.System.now()
        }

        // Extract location
        val location = asset.location
        val latitude = location?.coordinate?.latitude
        val longitude = location?.coordinate?.longitude

        // Extract dimensions
        val width = asset.pixelWidth.toInt()
        val height = asset.pixelHeight.toInt()

        // Determine MIME type from media subtype
        val mimeType = when (asset.mediaSubtypes) {
            PHAssetMediaSubtypePanorama -> "image/jpeg"
            PHAssetMediaSubtypeHDR -> "image/jpeg"
            PHAssetMediaSubtypeScreenshot -> "image/png"
            PHAssetMediaSubtypeLivePhoto -> "image/heic"
            else -> "image/jpeg"
        }

        return PhotoMetadata(
            uri = asset.localIdentifier,
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            width = width,
            height = height,
            sizeBytes = null,
            mimeType = mimeType
        )
    }
}
