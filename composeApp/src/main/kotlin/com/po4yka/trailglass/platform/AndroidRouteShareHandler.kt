package com.po4yka.trailglass.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.po4yka.trailglass.feature.route.export.RouteShareHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Android implementation of RouteShareHandler using Android sharing APIs. */
class AndroidRouteShareHandler(
    private val context: Context
) : RouteShareHandler {
    override suspend fun shareRouteFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // Write content to temporary file
                val cacheDir = File(context.cacheDir, "shared_routes")
                cacheDir.mkdirs()

                val file = File(cacheDir, fileName)
                file.writeText(content)

                // Get URI using FileProvider
                val uri =
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                // Create share intent
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Route: $fileName")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                // Show share sheet
                val chooser =
                    Intent.createChooser(intent, "Share Route").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(chooser)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun shareMapSnapshot(
        imageData: ByteArray,
        tripName: String
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // Write image to temporary file
                val cacheDir = File(context.cacheDir, "shared_images")
                cacheDir.mkdirs()

                val fileName = "${tripName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")}.png"
                val file = File(cacheDir, fileName)
                file.writeBytes(imageData)

                // Get URI using FileProvider
                val uri =
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                // Create share intent
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Route Map: $tripName")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                // Show share sheet
                val chooser =
                    Intent.createChooser(intent, "Share Map").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(chooser)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun saveRouteFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Write to app-specific storage
                val filesDir = File(context.filesDir, "exported_routes")
                filesDir.mkdirs()

                val file = File(filesDir, fileName)
                file.writeText(content)

                Result.success(file.absolutePath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
