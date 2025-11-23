package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation

/**
 * Compass-based reverse geocoder implementation (PLACEHOLDER).
 *
 * NOTE: This is a placeholder implementation. The Compass library integration requires platform-specific setup for
 * Android and iOS geocoding services.
 *
 * For full Compass integration, see the documentation at: https://compass.jordond.dev/docs/geocoding/
 *
 * Benefits of Compass (when fully integrated):
 * - Works in common code (reduced expect/actual)
 * - Consistent API across platforms
 * - Better error handling
 * - Built-in rate limiting
 *
 * Current state: Dependencies are added but platform-specific implementation is needed. The existing
 * AndroidReverseGeocoder and IOSReverseGeocoder remain the active implementations.
 *
 * TODO: Complete Compass integration with proper platform setup
 */
class CompassReverseGeocoder : ReverseGeocoder {
    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? {
        // Placeholder implementation
        // TODO: Implement Compass geocoding with proper platform configuration
        return null
    }
}

/**
 * Factory function to create a Compass-based reverse geocoder (placeholder).
 *
 * NOTE: This returns a placeholder. Use platform-specific geocoders for now:
 * - Android: createAndroidReverseGeocoder(context)
 * - iOS: createIOSReverseGeocoder()
 */
fun createCompassReverseGeocoder(): ReverseGeocoder = CompassReverseGeocoder()
