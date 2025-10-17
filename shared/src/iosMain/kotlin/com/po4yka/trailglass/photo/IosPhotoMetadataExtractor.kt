package com.po4yka.trailglass.photo

import com.po4yka.trailglass.domain.model.Location
import com.po4yka.trailglass.domain.model.PhotoMetadata
import com.po4yka.trailglass.logging.logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import platform.CoreLocation.CLLocation
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.Photos.*

/**
 * iOS implementation of photo metadata extractor using Photos framework.
 */
@OptIn(ExperimentalForeignApi::class)
@Inject
class IosPhotoMetadataExtractor : PhotoMetadataExtractor {

    private val logger = logger()

    override suspend fun extractMetadata(photoUri: String, photoId: String): PhotoMetadata? {
        return try {
            // photoUri on iOS is the PHAsset localIdentifier
            val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
                listOf(photoUri),
                null
            )

            if (fetchResult.count > 0u) {
                val asset = fetchResult.firstObject as? PHAsset
                asset?.let { extractFromAsset(it, photoId) }
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to extract metadata from iOS asset $photoUri" }
            null
        }
    }

    private fun extractFromAsset(asset: PHAsset, photoId: String): PhotoMetadata {
        // Extract location
        val location = asset.location as? CLLocation
        val latitude = location?.coordinate?.latitude
        val longitude = location?.coordinate?.longitude
        val altitude = location?.altitude?.takeIf { latitude != null && longitude != null }

        // Extract date/time
        val creationDate = asset.creationDate as? NSDate
        val exifTimestamp = creationDate?.let {
            Instant.fromEpochSeconds(it.timeIntervalSince1970.toLong())
        }

        val modificationDate = asset.modificationDate as? NSDate
        val modifiedTimestamp = modificationDate?.let {
            Instant.fromEpochSeconds(it.timeIntervalSince1970.toLong())
        }

        // Computed location
        val computedLocation = if (latitude != null && longitude != null) {
            Location(latitude, longitude, altitude)
        } else null

        val locationSource = when {
            latitude != null && longitude != null -> PhotoMetadata.LocationSource.EXIF
            else -> PhotoMetadata.LocationSource.NONE
        }

        // Note: iOS Photos framework doesn't expose all EXIF data easily
        // Would need to use ImageIO framework for full EXIF access
        return PhotoMetadata(
            photoId = photoId,
            exifLatitude = latitude,
            exifLongitude = longitude,
            exifAltitude = altitude,
            cameraMake = null, // Would need ImageIO
            cameraModel = null, // Would need ImageIO
            lens = null,
            focalLength = null,
            aperture = null,
            iso = null,
            shutterSpeed = null,
            flash = null,
            exifTimestamp = modifiedTimestamp,
            exifTimestampOriginal = exifTimestamp,
            orientation = null,
            colorSpace = null,
            computedLocation = computedLocation,
            locationSource = locationSource
        )
    }
}
