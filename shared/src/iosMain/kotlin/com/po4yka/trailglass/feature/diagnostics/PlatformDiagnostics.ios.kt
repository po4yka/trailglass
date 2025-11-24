package com.po4yka.trailglass.feature.diagnostics

import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIDevice
import platform.UserNotifications.UNUserNotificationCenter
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional

@OptIn(ExperimentalForeignApi::class)
actual class PlatformDiagnostics {
    actual suspend fun getSystemInfo(): SystemInfo {
        val bundle = NSBundle.mainBundle
        val appVersion = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "Unknown"
        val buildNumber = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "Unknown"
        val systemVersion = UIDevice.currentDevice.systemVersion
        val deviceModel = UIDevice.currentDevice.model

        return SystemInfo(
            appVersion = appVersion,
            buildNumber = buildNumber,
            osVersion = "iOS $systemVersion",
            deviceModel = deviceModel
        )
    }

    actual suspend fun getBatteryInfo(): BatteryInfo {
        val device = UIDevice.currentDevice
        device.batteryMonitoringEnabled = true

        val batteryLevel = if (device.batteryLevel >= 0) {
            device.batteryLevel
        } else {
            null
        }

        val processInfo = NSProcessInfo.processInfo
        val lowPowerMode = processInfo.lowPowerModeEnabled

        return BatteryInfo(
            batteryLevel = batteryLevel,
            batteryOptimizationDisabled = null,
            lowPowerMode = lowPowerMode
        )
    }

    actual suspend fun getPermissionsStatus(): PermissionsStatus {
        val locationManager = CLLocationManager()
        val authStatus = locationManager.authorizationStatus()

        val locationPermissionGranted = authStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
            authStatus == kCLAuthorizationStatusAuthorizedAlways

        val backgroundLocationPermissionGranted = authStatus == kCLAuthorizationStatusAuthorizedAlways

        var notificationsPermissionGranted = false
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            notificationsPermissionGranted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                settings?.authorizationStatus == UNAuthorizationStatusProvisional
        }

        val photoAuthStatus = PHPhotoLibrary.authorizationStatus()
        val photoLibraryPermissionGranted = photoAuthStatus == PHAuthorizationStatusAuthorized ||
            photoAuthStatus == PHAuthorizationStatusLimited

        return PermissionsStatus(
            locationPermissionGranted = locationPermissionGranted,
            backgroundLocationPermissionGranted = backgroundLocationPermissionGranted,
            notificationsPermissionGranted = notificationsPermissionGranted,
            photoLibraryPermissionGranted = photoLibraryPermissionGranted
        )
    }

    actual suspend fun getLocationInfo(): LocationInfo {
        val permissionsStatus = getPermissionsStatus()

        return LocationInfo(
            accuracy = null,
            satellites = null,
            locationPermissionGranted = permissionsStatus.locationPermissionGranted,
            backgroundLocationPermissionGranted = permissionsStatus.backgroundLocationPermissionGranted
        )
    }

    actual suspend fun getDatabaseSizeMB(): Double {
        val fileManager = NSFileManager.defaultManager
        val documentsDir = fileManager.URLsForDirectory(
            directory = platform.Foundation.NSDocumentDirectory,
            inDomains = platform.Foundation.NSUserDomainMask
        ).firstOrNull() as? platform.Foundation.NSURL

        val dbPath = documentsDir?.path?.let { "$it/trailglass.db" }

        return if (dbPath != null && fileManager.fileExistsAtPath(dbPath)) {
            val attributes = fileManager.attributesOfItemAtPath(dbPath, null)
            val fileSize = attributes?.get(platform.Foundation.NSFileSize) as? Long ?: 0L
            fileSize / (1024.0 * 1024.0)
        } else {
            0.0
        }
    }
}
