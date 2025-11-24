package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Compass-based reverse geocoder implementation.
 *
 * NOTE: This implementation requires platform-specific Compass geocoder setup.
 * The Compass library needs to be configured with platform-specific geocoding providers.
 *
 * For full Compass integration:
 * 1. Configure Compass geocoder in platform-specific code (Android/iOS)
 * 2. Pass the configured geocoder instance to this class
 * 3. See Compass documentation: https://compass.jordond.dev/docs/geocoding/
 *
 * Benefits of Compass (when fully integrated):
 * - Works in common code (reduced expect/actual)
 * - Consistent API across platforms
 * - Better error handling
 * - Built-in rate limiting
 *
 * Current state: Basic structure implemented. Platform-specific configuration needed.
 * The existing AndroidReverseGeocoder and IOSReverseGeocoder remain the active implementations.
 */
class CompassReverseGeocoder(
    // TODO: Add Compass geocoder instance when platform setup is complete
    // private val compassGeocoder: Geocoder? = null
) : ReverseGeocoder {
    private val logger = logger()

    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? {
        return withContext(Dispatchers.Default) {
            try {
                logger.trace { "Reverse geocoding ($latitude, $longitude) using Compass" }

                // TODO: Implement Compass geocoding when platform setup is complete
                // Example implementation (when geocoder is available):
                // val result = compassGeocoder?.reverseGeocode(
                //     latitude = latitude,
                //     longitude = longitude
                // )
                // result?.let { address ->
                //     GeocodedLocation(
                //         latitude = latitude,
                //         longitude = longitude,
                //         formattedAddress = address.formattedAddress,
                //         city = address.city,
                //         state = address.state,
                //         countryCode = address.countryCode,
                //         countryName = address.countryName,
                //         postalCode = address.postalCode,
                //         poiName = address.poiName,
                //         street = address.street,
                //         streetNumber = address.streetNumber
                //     )
                // }

                // For now, return null - platform geocoders are used instead
                logger.debug { "Compass geocoder not yet configured, returning null" }
                null
            } catch (e: Exception) {
                logger.error(e) { "Compass geocoding error for ($latitude, $longitude)" }
                null
            }
        }
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
