package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import kotlin.coroutines.resume

/**
 * iOS implementation of ReverseGeocoder using CoreLocation's CLGeocoder.
 */
class IOSReverseGeocoder : ReverseGeocoder {

    private val geocoder = CLGeocoder()
    private val logger = logger()

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedLocation? {
        logger.trace { "Reverse geocoding ($latitude, $longitude) using iOS CLGeocoder" }

        return suspendCancellableCoroutine { continuation ->
            val location = CLLocation(latitude = latitude, longitude = longitude)

            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                if (error != null) {
                    logger.error { "iOS CLGeocoder error for ($latitude, $longitude): ${error.localizedDescription}" }
                    continuation.resume(null)
                    return@reverseGeocodeLocation
                }

                @Suppress("UNCHECKED_CAST")
                val placemarkList = placemarks as? List<CLPlacemark>
                val placemark = placemarkList?.firstOrNull()

                if (placemark != null) {
                    val result = placemark.toGeocodedLocation(latitude, longitude)
                    logger.debug { "iOS CLGeocoder success: ${result.city ?: result.formattedAddress}" }
                    continuation.resume(result)
                } else {
                    logger.debug { "iOS CLGeocoder returned no results for ($latitude, $longitude)" }
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                logger.trace { "iOS CLGeocoder cancelled for ($latitude, $longitude)" }
                geocoder.cancelGeocode()
            }
        }
    }

    private fun CLPlacemark.toGeocodedLocation(lat: Double, lon: Double): GeocodedLocation {
        // Build formatted address from components
        val addressComponents = listOfNotNull(
            subThoroughfare,
            thoroughfare,
            locality,
            administrativeArea,
            postalCode,
            country
        )
        val formattedAddress = if (addressComponents.isNotEmpty()) {
            addressComponents.joinToString(", ")
        } else {
            null
        }

        return GeocodedLocation(
            latitude = lat,
            longitude = lon,
            formattedAddress = formattedAddress,
            city = locality,
            state = administrativeArea,
            countryCode = ISOcountryCode,
            countryName = country,
            postalCode = postalCode,
            poiName = name,
            street = thoroughfare,
            streetNumber = subThoroughfare
        )
    }
}

/**
 * Factory function to create IOSReverseGeocoder.
 */
actual fun createReverseGeocoder(): ReverseGeocoder {
    return IOSReverseGeocoder()
}
