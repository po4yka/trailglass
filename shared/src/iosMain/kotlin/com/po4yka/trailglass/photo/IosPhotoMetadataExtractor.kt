package com.po4yka.trailglass.photo

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.Location
import com.po4yka.trailglass.domain.model.PhotoMetadata
import com.po4yka.trailglass.logging.logger
import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import platform.CoreFoundation.CFDataRef
import platform.CoreLocation.CLLocation
import platform.Foundation.*
import platform.ImageIO.*
import platform.Photos.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of photo metadata extractor using Photos and ImageIO frameworks.
 * Provides full EXIF extraction including camera details.
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

    private suspend fun extractFromAsset(asset: PHAsset, photoId: String): PhotoMetadata {
        // Extract basic info from PHAsset
        val location = asset.location as? CLLocation
        val latitude = location?.coordinate?.useContents { latitude }
        val longitude = location?.coordinate?.useContents { longitude }
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

        // Extract full EXIF data using ImageIO
        val exifData = extractFullExifData(asset)

        // Computed location
        val computedLocation = if (latitude != null && longitude != null) {
            Location(Coordinate(latitude!!, longitude!!), altitude = altitude, timestamp = exifTimestamp ?: Clock.System.now())
        } else null

        val locationSource = when {
            latitude != null && longitude != null -> PhotoMetadata.LocationSource.EXIF
            else -> PhotoMetadata.LocationSource.NONE
        }

        return PhotoMetadata(
            photoId = photoId,
            exifLatitude = latitude,
            exifLongitude = longitude,
            exifAltitude = altitude,
            cameraMake = exifData.cameraMake,
            cameraModel = exifData.cameraModel,
            lens = exifData.lens,
            focalLength = exifData.focalLength,
            aperture = exifData.aperture,
            iso = exifData.iso,
            shutterSpeed = exifData.shutterSpeed,
            flash = exifData.flash,
            exifTimestamp = modifiedTimestamp,
            exifTimestampOriginal = exifTimestamp,
            orientation = exifData.orientation,
            colorSpace = exifData.colorSpace,
            computedLocation = computedLocation,
            locationSource = locationSource
        )
    }

    private suspend fun extractFullExifData(asset: PHAsset): ExifData = suspendCoroutine { continuation ->
        val options = PHImageRequestOptions().apply {
            version = PHImageRequestOptionsVersionCurrent
            deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat
            synchronous = false
            networkAccessAllowed = true
        }

        PHImageManager.defaultManager().requestImageDataForAsset(
            asset,
            options
        ) { imageData, dataUTI, orientation, info ->
            val exifData = if (imageData != null) {
                parseExifFromImageData(imageData)
            } else {
                ExifData()
            }
            continuation.resume(exifData)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun parseExifFromImageData(imageData: NSData): ExifData {
        return try {
            // Create image source from data
            val imageSource = CGImageSourceCreateWithData(imageData as CFDataRef, null)
                ?: return ExifData()

            // Get metadata dictionary at index 0
            val metadata = CGImageSourceCopyPropertiesAtIndex(imageSource, 0u, null)
                ?: return ExifData()

            val metadataDict = metadata as NSDictionary

            // Extract EXIF dictionary
            val exifDict = metadataDict.objectForKey(kCGImagePropertyExifDictionary) as? NSDictionary

            // Extract TIFF dictionary (for camera make/model)
            val tiffDict = metadataDict.objectForKey(kCGImagePropertyTIFFDictionary) as? NSDictionary

            // Extract camera make and model
            val cameraMake = tiffDict?.objectForKey("Make") as? String
            val cameraModel = tiffDict?.objectForKey("Model") as? String

            // Extract lens info
            val lens = exifDict?.objectForKey(kCGImagePropertyExifLensModel) as? String

            // Extract focal length
            val focalLength = exifDict?.objectForKey(kCGImagePropertyExifFocalLength) as? NSNumber
            val focalLengthValue = focalLength?.doubleValue

            // Extract aperture (F-number)
            val aperture = exifDict?.objectForKey(kCGImagePropertyExifFNumber) as? NSNumber
            val apertureValue = aperture?.doubleValue

            // Extract ISO
            val isoArray = exifDict?.objectForKey(kCGImagePropertyExifISOSpeedRatings) as? NSArray
            val iso = if (isoArray != null && isoArray.count > 0u) {
                (isoArray.objectAtIndex(0u) as? NSNumber)?.intValue
            } else null

            // Extract shutter speed (exposure time)
            val exposureTime = exifDict?.objectForKey(kCGImagePropertyExifExposureTime) as? NSNumber
            val shutterSpeed = exposureTime?.let {
                val time = it.doubleValue
                if (time >= 1.0) {
                    "${time.toInt()}s"
                } else {
                    "1/${(1.0 / time).toInt()}"
                }
            }

            // Extract flash
            val flash = exifDict?.objectForKey(kCGImagePropertyExifFlash) as? NSNumber
            val flashValue = flash?.intValue?.let { it != 0 }

            // Extract orientation
            val orientationNum = tiffDict?.objectForKey(kCGImagePropertyOrientation) as? NSNumber
            val orientation = orientationNum?.intValue

            // Extract color space
            val colorSpace = exifDict?.objectForKey(kCGImagePropertyExifColorSpace) as? NSNumber
            val colorSpaceName = when (colorSpace?.intValue) {
                1 -> "sRGB"
                65535 -> "Uncalibrated"
                else -> null
            }

            ExifData(
                cameraMake = cameraMake,
                cameraModel = cameraModel,
                lens = lens,
                focalLength = focalLengthValue,
                aperture = apertureValue,
                iso = iso,
                shutterSpeed = shutterSpeed,
                flash = flashValue,
                orientation = orientation,
                colorSpace = colorSpaceName
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse EXIF data from ImageIO" }
            ExifData()
        }
    }

    /**
     * Container for extracted EXIF data.
     */
    private data class ExifData(
        val cameraMake: String? = null,
        val cameraModel: String? = null,
        val lens: String? = null,
        val focalLength: Double? = null,
        val aperture: Double? = null,
        val iso: Int? = null,
        val shutterSpeed: String? = null,
        val flash: Boolean? = null,
        val orientation: Int? = null,
        val colorSpace: String? = null
    )
}
