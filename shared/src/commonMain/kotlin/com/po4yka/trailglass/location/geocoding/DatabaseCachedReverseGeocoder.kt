package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation

/**
 * Wrapper around ReverseGeocoder that adds database-backed caching.
 * Reduces API calls and persists results across app restarts.
 */
class DatabaseCachedReverseGeocoder(
    private val geocoder: ReverseGeocoder,
    private val cache: DatabaseGeocodingCache
) : ReverseGeocoder {

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedLocation? {
        // Check cache first
        cache.get(latitude, longitude)?.let { return it }

        // If not cached, perform actual geocoding
        val result = geocoder.reverseGeocode(latitude, longitude)

        // Cache the result if successful
        result?.let { cache.put(it) }

        return result
    }

    /**
     * Clear expired cache entries.
     */
    suspend fun clearExpiredCache() {
        cache.clearExpired()
    }

    /**
     * Clear all cache entries.
     */
    suspend fun clearCache() {
        cache.clear()
    }

    /**
     * Get count of cached entries.
     */
    suspend fun getCacheCount(): Long {
        return cache.count()
    }
}
