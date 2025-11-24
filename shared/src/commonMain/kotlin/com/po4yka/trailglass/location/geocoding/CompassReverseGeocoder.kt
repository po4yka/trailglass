package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation
import com.po4yka.trailglass.logging.logger
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.mobile.Geocoder as MobileGeocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Compass-based reverse geocoder implementation.
 *
 * This implementation uses Compass library for cross-platform geocoding.
 * Compass provides a unified API that works in common code, reducing the need for expect/actual.
 *
 * Benefits:
 * - Works in common code (no platform-specific expect/actual needed)
 * - Consistent API across platforms
 * - Better error handling and rate limiting
 * - Built-in caching and retry logic
 *
 * The geocoder instance is created in platform-specific code but used here in common code.
 */
class CompassReverseGeocoder(
    private val compassGeocoder: Geocoder
) : ReverseGeocoder {
    private val logger = logger()

    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? {
        return withContext(Dispatchers.Default) {
            try {
                logger.trace { "Reverse geocoding ($latitude, $longitude) using Compass" }

                val result = compassGeocoder.reverseGeocode(
                    latitude = latitude,
                    longitude = longitude
                )

                result?.let { address ->
                    logger.debug { "Compass geocoding successful: ${address.formattedAddress}" }
                    GeocodedLocation(
                        latitude = latitude,
                        longitude = longitude,
                        formattedAddress = address.formattedAddress,
                        city = address.city,
                        state = address.state,
                        countryCode = address.countryCode,
                        countryName = address.countryName,
                        postalCode = address.postalCode,
                        poiName = address.poiName,
                        street = address.street,
                        streetNumber = address.streetNumber
                    )
                } ?: run {
                    logger.debug { "Compass geocoding returned no result for ($latitude, $longitude)" }
                    null
                }
            } catch (e: Exception) {
                logger.error(e) { "Compass geocoding error for ($latitude, $longitude)" }
                null
            }
        }
    }
}

/**
 * Factory function to create a Compass-based reverse geocoder.
 *
 * This should be called from platform-specific code after configuring the Compass geocoder.
 * The geocoder instance should be passed to the constructor.
 *
 * Example usage:
 * ```kotlin
 * // In Android platform code
 * val geocoder = MobileGeocoder.create(context)
 * val compassGeocoder = CompassReverseGeocoder(geocoder)
 * ```
 */
fun createCompassReverseGeocoder(geocoder: Geocoder): ReverseGeocoder =
    CompassReverseGeocoder(geocoder)
