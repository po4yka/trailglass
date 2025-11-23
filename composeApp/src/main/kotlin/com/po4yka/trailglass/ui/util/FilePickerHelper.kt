package com.po4yka.trailglass.ui.util

import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.po4yka.trailglass.feature.export.ExportDataUseCase
import java.io.File

/** Helper for Android file picking and export file creation. Handles platform-specific file access patterns. */
class FilePickerHelper(
    private val activity: ComponentActivity
) {
    private var onFileSelected: ((String) -> Unit)? = null

    private val createDocumentLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            uri?.let { fileUri ->
                val path = getPathFromUri(fileUri)
                path?.let { onFileSelected?.invoke(it) }
            }
        }

    /**
     * Request file creation for export. Opens the system file picker to let user choose where to save the file.
     *
     * @param format Export format to determine file extension
     * @param defaultFileName Default name for the exported file
     * @param onSelected Callback with the selected file path
     */
    fun requestExportFile(
        format: ExportDataUseCase.Format,
        defaultFileName: String,
        onSelected: (String) -> Unit
    ) {
        onFileSelected = onSelected

        val extension =
            when (format) {
                ExportDataUseCase.Format.CSV -> "csv"
                ExportDataUseCase.Format.GPX -> "gpx"
                ExportDataUseCase.Format.JSON -> "json"
            }

        val fileName =
            if (defaultFileName.endsWith(".$extension")) {
                defaultFileName
            } else {
                "$defaultFileName.$extension"
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use Storage Access Framework for Android 10+
            createDocumentLauncher.launch(fileName)
        } else {
            // Use legacy external storage for older versions
            val exportDir =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Trailglass/Exports"
                )
            exportDir.mkdirs()
            val file = File(exportDir, fileName)
            onSelected(file.absolutePath)
        }
    }

    /**
     * Get default export directory for the app. Returns a path that's accessible without SAF on older Android versions.
     */
    fun getDefaultExportDirectory(): File =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, use app-specific external storage
            File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Exports")
        } else {
            // For older versions, use public downloads directory
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Trailglass/Exports"
            )
        }.apply {
            mkdirs()
        }

    /** Generate a default file name for export based on data type and format. */
    fun generateDefaultFileName(
        dataTypeName: String,
        format: ExportDataUseCase.Format,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val extension =
            when (format) {
                ExportDataUseCase.Format.CSV -> "csv"
                ExportDataUseCase.Format.GPX -> "gpx"
                ExportDataUseCase.Format.JSON -> "json"
            }

        val sanitizedName =
            dataTypeName
                .replace(Regex("[^a-zA-Z0-9-_]"), "_")
                .take(50)

        return "trailglass_${sanitizedName}_$timestamp.$extension"
    }

    /**
     * Convert URI to file path. This is a simplified version that works with most URIs. For production use, consider
     * using a more robust solution.
     */
    private fun getPathFromUri(uri: Uri): String? =
        try {
            // For content:// URIs, we need to use contentResolver
            if (uri.scheme == "content") {
                // Use the file descriptor to copy to app-specific storage
                val fileName = getFileNameFromUri(uri) ?: "export_${System.currentTimeMillis()}"
                val exportDir = getDefaultExportDirectory()
                val destinationFile = File(exportDir, fileName)

                activity.contentResolver.openInputStream(uri)?.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                destinationFile.absolutePath
            } else {
                // For file:// URIs, return the path directly
                uri.path
            }
        } catch (e: Exception) {
            null
        }

    /** Get file name from content URI. */
    private fun getFileNameFromUri(uri: Uri): String? =
        activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
}
