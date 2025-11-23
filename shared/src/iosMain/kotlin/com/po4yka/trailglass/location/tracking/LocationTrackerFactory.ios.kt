package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.repository.LocationRepository
import platform.UIKit.UIDevice

/** iOS implementation of LocationTrackerFactory. */
actual class LocationTrackerFactory {
    actual fun create(
        repository: LocationRepository,
        configuration: TrackingConfiguration,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    ): LocationTracker {
        // Get device ID from iOS
        val deviceId = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"

        val userId = DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous"

        return IOSLocationTracker(
            repository = repository,
            configuration = configuration,
            deviceId = deviceId,
            userId = userId,
            coroutineScope = coroutineScope
        )
    }
}
