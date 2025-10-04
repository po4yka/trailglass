package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.repository.LocationRepository
import platform.UIKit.UIDevice

/**
 * iOS implementation of LocationTrackerFactory.
 */
actual class LocationTrackerFactory {
    actual fun create(
        repository: LocationRepository,
        configuration: TrackingConfiguration
    ): LocationTracker {
        // Get device ID from iOS
        val deviceId = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"

        // TODO: Get userId from user session/auth
        val userId = "user_1"

        return IOSLocationTracker(
            repository = repository,
            configuration = configuration,
            deviceId = deviceId,
            userId = userId
        )
    }
}
