package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation

/**
 * Platform-specific reverse geocoder interface.
 * Converts coordinates to human-readable addresses.
 */
interface ReverseGeocoder {
    /**
     * Reverse geocode a coordinate to get address information.
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return GeocodedLocation with address details, or null if geocoding fails
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedLocation?
}

/**
 * Factory function to create platform-specific ReverseGeocoder.
 * This is implemented using expect/actual pattern.
 */
expect fun createReverseGeocoder(): ReverseGeocoder
