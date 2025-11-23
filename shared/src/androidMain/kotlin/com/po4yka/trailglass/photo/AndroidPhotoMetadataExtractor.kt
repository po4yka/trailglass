package com.po4yka.trailglass.photo

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.po4yka.trailglass.domain.model.Location
import com.po4yka.trailglass.domain.model.PhotoMetadata
import com.po4yka.trailglass.domain.model.PhotoMetadata.LocationSource
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Android implementation of photo metadata extractor using EXIF. */
@Inject
class AndroidPhotoMetadataExtractor(
    private val context: Context
) : PhotoMetadataExtractor {
    private val logger = logger()

    override suspend fun extractMetadata(
        photoUri: String,
        photoId: String
    ): PhotoMetadata? {
        return try {
            val uri = Uri.parse(photoUri)
            val inputStream =
                context.contentResolver.openInputStream(uri)
                    ?: return null

            inputStream.use { stream ->
                val exif = ExifInterface(stream)
                extractFromExif(exif, photoId)
            }
        } catch (e: java.io.IOException) {
            logger.error(e) { "Failed to read EXIF metadata from $photoUri" }
            null
        } catch (e: SecurityException) {
            logger.error(e) { "Permission denied to read photo at $photoUri" }
            null
        }
    }

    private fun extractFromExif(
        exif: ExifInterface,
        photoId: String
    ): PhotoMetadata {
        // Extract location using the non-deprecated latLong property
        val latLongPair = exif.latLong
        val latitude = latLongPair?.get(0)
        val longitude = latLongPair?.get(1)
        val hasLocation = latitude != null && longitude != null
        val altitude =
            exif
                .getAttributeDouble(ExifInterface.TAG_GPS_ALTITUDE, 0.0)
                .takeIf { hasLocation && it != 0.0 }

        // Extract camera info
        val cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE)
        val cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL)
        val lens = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)

        // Extract camera settings
        val focalLength =
            exif
                .getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
                .takeIf { it != 0.0 }
        val aperture =
            exif
                .getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0)
                .takeIf { it != 0.0 }
        val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)?.toIntOrNull()
        val exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
        val shutterSpeed = exposureTime?.let { formatShutterSpeed(it) }
        val flash = exif.getAttribute(ExifInterface.TAG_FLASH)?.toIntOrNull()?.let { it != 0 }

        // Extract date/time
        val dateTimeOriginal =
            exif
                .getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?.let { parseExifDateTime(it) }
        val dateTime =
            exif
                .getAttribute(ExifInterface.TAG_DATETIME)
                ?.let { parseExifDateTime(it) }

        // Extract image properties
        val orientation =
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        val colorSpace = exif.getAttribute(ExifInterface.TAG_COLOR_SPACE)

        // Computed location
        val computedLocation =
            if (latitude != null && longitude != null) {
                Location(latitude, longitude, null, altitude)
            } else {
                null
            }

        val locationSource =
            when {
                latitude != null && longitude != null -> LocationSource.EXIF
                else -> LocationSource.NONE
            }

        return PhotoMetadata(
            photoId = photoId,
            exifLatitude = latitude,
            exifLongitude = longitude,
            exifAltitude = altitude,
            cameraMake = cameraMake,
            cameraModel = cameraModel,
            lens = lens,
            focalLength = focalLength,
            aperture = aperture,
            iso = iso,
            shutterSpeed = shutterSpeed,
            flash = flash,
            exifTimestamp = dateTime,
            exifTimestampOriginal = dateTimeOriginal,
            orientation = orientation,
            colorSpace = colorSpace,
            computedLocation = computedLocation,
            locationSource = locationSource
        )
    }

    /** Parse EXIF date time string to Instant. Format: "YYYY:MM:DD HH:MM:SS" */
    private fun parseExifDateTime(dateTimeStr: String): Instant? =
        try {
            val format = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getDefault()
            val date = format.parse(dateTimeStr)
            date?.let { Instant.fromEpochMilliseconds(it.time) }
        } catch (e: java.text.ParseException) {
            logger.warn(e) { "Failed to parse EXIF datetime: $dateTimeStr" }
            null
        }

    /** Format exposure time as shutter speed (e.g., "1/250"). */
    private fun formatShutterSpeed(exposureTime: String): String =
        try {
            val value = exposureTime.toDouble()
            when {
                value >= 1 -> "${value.toInt()}s"
                else -> "1/${(1 / value).toInt()}"
            }
        } catch (e: NumberFormatException) {
            exposureTime
        }
}
