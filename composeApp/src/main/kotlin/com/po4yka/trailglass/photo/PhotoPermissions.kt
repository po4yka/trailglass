package com.po4yka.trailglass.photo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/** Helper class for managing photo library permissions. */
class PhotoPermissions(
    private val activity: ComponentActivity
) {
    private var onPermissionsResult: ((Boolean) -> Unit)? = null

    private val permissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            onPermissionsResult?.invoke(allGranted)
            onPermissionsResult = null
        }

    /** Check if photo library permissions are granted. */
    fun hasPhotoPermissions(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular media permissions
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    /** Request photo library permissions. */
    fun requestPhotoPermissions(onResult: (Boolean) -> Unit) {
        onPermissionsResult = onResult

        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        permissionLauncher.launch(permissions)
    }

    /** Check if we should show rationale for photo permissions. */
    fun shouldShowPhotoRationale(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /** Open app settings to allow user to manually grant permissions. */
    fun openAppSettings() {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        activity.startActivity(intent)
    }

    companion object {
        /** Get required permissions for photo access based on Android version. */
        fun getRequiredPermissions(): List<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
    }
}
