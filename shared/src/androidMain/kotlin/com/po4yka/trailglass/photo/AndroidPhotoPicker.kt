package com.po4yka.trailglass.photo

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Android implementation of PhotoPicker using MediaStore.
 * Note: This class handles metadata extraction but requires Activity-based
 * photo selection to be handled by the UI layer.
 */
class AndroidPhotoPicker(
    private val context: Context,
    private val userId: String
) : PhotoPicker {

    private val logger = logger()
    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun pickPhoto(): Photo? {
        logger.warn { "pickPhoto() must be called from Activity with launcher" }
        return null
    }

    override suspend fun pickPhotos(maxPhotos: Int): List<Photo> {
        logger.warn { "pickPhotos() must be called from Activity with launcher" }
        return emptyList()
    }

    override suspend fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular media permissions
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override suspend fun requestPermissions(): Boolean {
        logger.warn { "requestPermissions() must be called from Activity" }
        return false
    }

    /**
     * Extract photo metadata from a URI.
     * This is called after the user has selected a photo via Activity Result API.
     */
    suspend fun extractPhotoFromUri(uri: Uri): Photo? = withContext(Dispatchers.IO) {
        logger.debug { "Extracting photo metadata from URI: $uri" }

        try {
            val metadata = extractMetadataFromUri(uri)
            if (metadata == null) {
                logger.warn { "Failed to extract metadata from URI: $uri" }
                return@withContext null
            }

            Photo(
                id = "photo_${UUID.randomUUID()}",
                uri = uri.toString(),
                timestamp = metadata.timestamp,
                latitude = metadata.latitude,
                longitude = metadata.longitude,
                width = metadata.width,
                height = metadata.height,
                sizeBytes = metadata.sizeBytes,
                mimeType = metadata.mimeType,
                userId = userId,
                addedAt = Clock.System.now()
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to extract photo from URI: $uri" }
            null
        }
    }

    /**
     * Extract multiple photos from URIs.
     */
    suspend fun extractPhotosFromUris(uris: List<Uri>): List<Photo> = withContext(Dispatchers.IO) {
        logger.debug { "Extracting ${uris.size} photos from URIs" }

        uris.mapNotNull { uri ->
            extractPhotoFromUri(uri)
        }
    }

    /**
     * Extract metadata from a content URI using MediaStore.
     */
    private fun extractMetadataFromUri(uri: Uri): PhotoMetadata? {
        val projection = arrayOf(
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE
        )

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)
                val lonIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE)
                val widthIndex = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightIndex = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)

                val dateTaken = if (dateTakenIndex >= 0) cursor.getLong(dateTakenIndex) else null
                val latitude = if (latIndex >= 0) {
                    val lat = cursor.getDouble(latIndex)
                    if (lat != 0.0) lat else null
                } else null
                val longitude = if (lonIndex >= 0) {
                    val lon = cursor.getDouble(lonIndex)
                    if (lon != 0.0) lon else null
                } else null
                val width = if (widthIndex >= 0) cursor.getInt(widthIndex) else null
                val height = if (heightIndex >= 0) cursor.getInt(heightIndex) else null
                val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else null
                val mimeType = if (mimeTypeIndex >= 0) cursor.getString(mimeTypeIndex) else null

                val timestamp = if (dateTaken != null && dateTaken > 0) {
                    Instant.fromEpochMilliseconds(dateTaken)
                } else {
                    Clock.System.now()
                }

                return PhotoMetadata(
                    uri = uri.toString(),
                    timestamp = timestamp,
                    latitude = latitude,
                    longitude = longitude,
                    width = width,
                    height = height,
                    sizeBytes = size,
                    mimeType = mimeType
                )
            }
        }

        return null
    }

    companion object {
        /**
         * Get required permissions for photo access based on Android version.
         */
        fun getRequiredPermissions(): List<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}
