package com.po4yka.trailglass.location.geocoding

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.po4yka.trailglass.domain.model.GeocodedLocation
import com.po4yka.trailglass.logging.logger
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
    private val logger = logger()

    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? {
        if (!Geocoder.isPresent()) {
            logger.warn { "Android Geocoder is not present on this device" }
            return null
        }

        logger.trace { "Reverse geocoding ($latitude, $longitude) using Android Geocoder" }

        return try {
            val result =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use new async API on API 33+
                    logger.trace { "Using Android Geocoder async API (SDK ${Build.VERSION.SDK_INT})" }
                    reverseGeocodeAsync(latitude, longitude)
                } else {
                    // Use legacy blocking API on older versions
                    logger.trace { "Using Android Geocoder legacy API (SDK ${Build.VERSION.SDK_INT})" }
                    reverseGeocodeLegacy(latitude, longitude)
                }

            if (result != null) {
                logger.debug { "Android Geocoder success: ${result.city ?: result.formattedAddress}" }
            } else {
                logger.debug { "Android Geocoder returned no results for ($latitude, $longitude)" }
            }
            result
        } catch (e: IOException) {
            logger.error(e) { "Android Geocoder network/service error for ($latitude, $longitude)" }
            null
        } catch (e: Exception) {
            logger.error(e) { "Android Geocoder unexpected error for ($latitude, $longitude)" }
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocodeLegacy(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses?.firstOrNull()?.toGeocodedLocation(latitude, longitude)
    }

    private suspend fun reverseGeocodeAsync(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? {
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

    private fun Address.toGeocodedLocation(
        lat: Double,
        lon: Double
    ): GeocodedLocation =
        GeocodedLocation(
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

/**
 * Factory function to create AndroidReverseGeocoder.
 * Requires Android Context.
 */
actual fun createReverseGeocoder(): ReverseGeocoder =
    throw IllegalStateException(
        "createReverseGeocoder() requires Android Context. " +
            "Use createAndroidReverseGeocoder(context) instead."
    )

/**
 * Android-specific factory function that accepts Context.
 */
fun createAndroidReverseGeocoder(context: Context): ReverseGeocoder = AndroidReverseGeocoder(context)
