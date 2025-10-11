package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.data.repository.LocationRepository

/**
 * Factory for creating platform-specific LocationTracker instances.
 */
expect class LocationTrackerFactory {
    /**
     * Create a LocationTracker instance for the current platform.
     *
     * @param repository Repository for storing location samples
     * @param configuration Tracking configuration
     * @return Platform-specific LocationTracker implementation
     */
    fun create(
        repository: LocationRepository,
        configuration: TrackingConfiguration = TrackingConfigurations.BALANCED
    ): LocationTracker
}
