package com.po4yka.trailglass.location.geocoding

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.po4yka.trailglass.domain.model.GeocodedLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

/**
 * Android implementation of ReverseGeocoder using Android's Geocoder API.
 */
class AndroidReverseGeocoder(
    private val context: Context
) : ReverseGeocoder {

    private val geocoder = Geocoder(context)

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedLocation? {
        if (!Geocoder.isPresent()) {
            return null
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new async API on API 33+
                reverseGeocodeAsync(latitude, longitude)
            } else {
                // Use legacy blocking API on older versions
                reverseGeocodeLegacy(latitude, longitude)
            }
        } catch (e: IOException) {
            // Network or service error
            null
        } catch (e: Exception) {
            // Other errors
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocodeLegacy(latitude: Double, longitude: Double): GeocodedLocation? {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses?.firstOrNull()?.toGeocodedLocation(latitude, longitude)
    }

    private suspend fun reverseGeocodeAsync(latitude: Double, longitude: Double): GeocodedLocation? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return reverseGeocodeLegacy(latitude, longitude)
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                ) { addresses ->
                    val result = addresses.firstOrNull()?.toGeocodedLocation(latitude, longitude)
                    continuation.resume(result)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    private fun Address.toGeocodedLocation(lat: Double, lon: Double): GeocodedLocation {
        return GeocodedLocation(
            latitude = lat,
            longitude = lon,
            formattedAddress = getAddressLine(0),
            city = locality ?: subAdminArea,
            state = adminArea,
            countryCode = countryCode,
            countryName = countryName,
            postalCode = postalCode,
            poiName = featureName,
            street = thoroughfare,
            streetNumber = subThoroughfare
        )
    }
}

/**
 * Factory function to create AndroidReverseGeocoder.
 * Requires Android Context.
 */
actual fun createReverseGeocoder(): ReverseGeocoder {
    throw IllegalStateException(
        "createReverseGeocoder() requires Android Context. " +
        "Use createAndroidReverseGeocoder(context) instead."
    )
}

/**
 * Android-specific factory function that accepts Context.
 */
fun createAndroidReverseGeocoder(context: Context): ReverseGeocoder {
    return AndroidReverseGeocoder(context)
}
