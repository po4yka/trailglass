package com.po4yka.trailglass.feature.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import java.io.File

class AndroidPlatformDiagnostics(
    private val context: Context
) : PlatformDiagnostics {
    override suspend fun getSystemInfo(): SystemInfo {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return SystemInfo(
            appVersion = packageInfo.versionName ?: "Unknown",
            buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            },
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        )
    }

    override suspend fun getBatteryInfo(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        val batteryLevel = batteryManager?.let {
            val level = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (level > 0) level / 100f else null
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryOptimizationDisabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            null
        }

        val lowPowerMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager?.isPowerSaveMode ?: false
        } else {
            null
        }

        return BatteryInfo(
            batteryLevel = batteryLevel,
            batteryOptimizationDisabled = batteryOptimizationDisabled,
            lowPowerMode = lowPowerMode
        )
    }

    override suspend fun getPermissionsStatus(): PermissionsStatus {
        val locationPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            locationPermissionGranted
        }

        val notificationsPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val photoLibraryPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        return PermissionsStatus(
            locationPermissionGranted = locationPermissionGranted,
            backgroundLocationPermissionGranted = backgroundLocationPermissionGranted,
            notificationsPermissionGranted = notificationsPermissionGranted,
            photoLibraryPermissionGranted = photoLibraryPermissionGranted
        )
    }

    override suspend fun getLocationInfo(): LocationInfo {
        val permissionsStatus = getPermissionsStatus()

        return LocationInfo(
            accuracy = null,
            satellites = null,
            locationPermissionGranted = permissionsStatus.locationPermissionGranted,
            backgroundLocationPermissionGranted = permissionsStatus.backgroundLocationPermissionGranted
        )
    }

    override suspend fun getDatabaseSizeMB(): Double {
        val dbFile = context.getDatabasePath("trailglass.db")
        return if (dbFile?.exists() == true) {
            dbFile.length() / (1024.0 * 1024.0)
        } else {
            0.0
        }
    }
}
