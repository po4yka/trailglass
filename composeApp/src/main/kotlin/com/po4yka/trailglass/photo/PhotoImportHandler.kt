package com.po4yka.trailglass.photo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.po4yka.trailglass.feature.photo.ImportPhotoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PhotoImportHandler"

/**
 * Handler for photo import operations on Android. Manages photo picker integration and coordinates with
 * ImportPhotoUseCase.
 */
class PhotoImportHandler(
    private val context: Context,
    private val importPhotoUseCase: ImportPhotoUseCase,
    private val userId: String,
    private val onImportSuccess: (String) -> Unit,
    private val onImportError: (String) -> Unit
) {
    /**
     * Import a photo from a content URI.
     *
     * @param uri Content URI from photo picker
     */
    suspend fun importPhotoFromUri(uri: Uri) {
        Log.i(TAG, "Importing photo from URI: $uri")

        try {
            val photoData = readPhotoBytes(uri)
            if (photoData == null) {
                Log.e(TAG, "Failed to read photo data from URI: $uri")
                onImportError("Failed to read photo data")
                return
            }

            val result =
                importPhotoUseCase.execute(
                    uri = uri.toString(),
                    photoData = photoData,
                    userId = userId
                )

            when (result) {
                is ImportPhotoUseCase.ImportResult.Success -> {
                    Log.i(TAG, "Successfully imported photo ${result.photo.id}")
                    onImportSuccess(result.photo.id)
                }

                is ImportPhotoUseCase.ImportResult.Error -> {
                    Log.e(TAG, "Failed to import photo: ${result.message}")
                    onImportError(result.message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception importing photo from $uri", e)
            onImportError(e.message ?: "Unknown error")
        }
    }

    /**
     * Import multiple photos from content URIs.
     *
     * @param uris List of content URIs from photo picker
     */
    suspend fun importPhotosFromUris(uris: List<Uri>) {
        Log.i(TAG, "Importing ${uris.size} photos")

        var successCount = 0
        var errorCount = 0

        for (uri in uris) {
            try {
                val photoData = readPhotoBytes(uri)
                if (photoData == null) {
                    Log.e(TAG, "Failed to read photo data from URI: $uri")
                    errorCount++
                    continue
                }

                val result =
                    importPhotoUseCase.execute(
                        uri = uri.toString(),
                        photoData = photoData,
                        userId = userId
                    )

                when (result) {
                    is ImportPhotoUseCase.ImportResult.Success -> {
                        Log.i(TAG, "Successfully imported photo ${result.photo.id}")
                        successCount++
                    }

                    is ImportPhotoUseCase.ImportResult.Error -> {
                        Log.e(TAG, "Failed to import photo: ${result.message}")
                        errorCount++
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception importing photo from $uri", e)
                errorCount++
            }
        }

        Log.i(TAG, "Import complete: $successCount succeeded, $errorCount failed")

        if (successCount > 0) {
            onImportSuccess("Imported $successCount photo(s)")
        }
        if (errorCount > 0) {
            onImportError("Failed to import $errorCount photo(s)")
        }
    }

    /**
     * Read photo bytes from a content URI.
     *
     * @param uri Content URI to read
     * @return Photo bytes, or null if reading failed
     */
    private suspend fun readPhotoBytes(uri: Uri): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read bytes from URI: $uri", e)
                null
            }
        }
}

/**
 * Composable that creates a photo picker launcher and handler.
 *
 * @param importPhotoUseCase Use case for importing photos
 * @param userId Current user ID
 * @param onImportSuccess Callback when import succeeds
 * @param onImportError Callback when import fails
 * @return Pair of photo picker launcher and import handler
 */
@Composable
fun rememberPhotoImportHandler(
    importPhotoUseCase: ImportPhotoUseCase,
    userId: String,
    onImportSuccess: (String) -> Unit,
    onImportError: (String) -> Unit
): Pair<ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>, PhotoImportHandler> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val handler =
        remember(importPhotoUseCase, userId) {
            PhotoImportHandler(
                context = context,
                importPhotoUseCase = importPhotoUseCase,
                userId = userId,
                onImportSuccess = onImportSuccess,
                onImportError = onImportError
            )
        }

    val photoPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    handler.importPhotoFromUri(uri)
                }
            }
        }

    return Pair(photoPicker, handler)
}

/**
 * Composable that creates a multiple photo picker launcher and handler.
 *
 * @param importPhotoUseCase Use case for importing photos
 * @param userId Current user ID
 * @param maxPhotos Maximum number of photos to select
 * @param onImportSuccess Callback when import succeeds
 * @param onImportError Callback when import fails
 * @return Pair of photo picker launcher and import handler
 */
@Composable
fun rememberMultiplePhotoImportHandler(
    importPhotoUseCase: ImportPhotoUseCase,
    userId: String,
    maxPhotos: Int = 10,
    onImportSuccess: (String) -> Unit,
    onImportError: (String) -> Unit
): Pair<ManagedActivityResultLauncher<PickVisualMediaRequest, List<Uri>>, PhotoImportHandler> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val handler =
        remember(importPhotoUseCase, userId) {
            PhotoImportHandler(
                context = context,
                importPhotoUseCase = importPhotoUseCase,
                userId = userId,
                onImportSuccess = onImportSuccess,
                onImportError = onImportError
            )
        }

    val photoPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxPhotos)
        ) { uris ->
            if (uris.isNotEmpty()) {
                scope.launch {
                    handler.importPhotosFromUris(uris)
                }
            }
        }

    return Pair(photoPicker, handler)
}
