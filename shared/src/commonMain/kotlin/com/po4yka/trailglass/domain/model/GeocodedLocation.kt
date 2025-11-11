package com.po4yka.trailglass.domain.model

/**
 * Result of reverse geocoding a coordinate.
 * Contains human-readable address components.
 */
data class GeocodedLocation(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String? = null,
    val city: String? = null,
    val state: String? = null,
    val countryCode: String? = null,
    val countryName: String? = null,
    val postalCode: String? = null,
    val poiName: String? = null,
    val street: String? = null,
    val streetNumber: String? = null
)
