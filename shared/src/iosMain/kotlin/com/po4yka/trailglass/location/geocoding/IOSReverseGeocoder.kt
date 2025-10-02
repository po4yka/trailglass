package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import kotlin.coroutines.resume

/**
 * iOS implementation of ReverseGeocoder using CoreLocation's CLGeocoder.
 */
class IOSReverseGeocoder : ReverseGeocoder {

    private val geocoder = CLGeocoder()

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedLocation? {
        return suspendCancellableCoroutine { continuation ->
            val location = CLLocation(latitude = latitude, longitude = longitude)

            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                if (error != null) {
                    continuation.resume(null)
                    return@reverseGeocodeLocation
                }

                @Suppress("UNCHECKED_CAST")
                val placemarkList = placemarks as? List<CLPlacemark>
                val placemark = placemarkList?.firstOrNull()

                if (placemark != null) {
                    val result = placemark.toGeocodedLocation(latitude, longitude)
                    continuation.resume(result)
                } else {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
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
