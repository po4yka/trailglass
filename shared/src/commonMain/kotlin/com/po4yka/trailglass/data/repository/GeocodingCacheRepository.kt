package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.GeocodedLocation

/**
 * Repository for managing geocoding cache.
 */
interface GeocodingCacheRepository {
    /**
     * Get a cached geocoded location near the specified coordinates.
     * Uses spatial proximity matching.
     *
     * @param latitude The latitude to search near
     * @param longitude The longitude to search near
     * @param radiusMeters The search radius in meters (default 100m)
     * @return Cached location if found within radius, null otherwise
     */
    suspend fun get(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 100.0
    ): GeocodedLocation?

    /**
     * Store a geocoded location in the cache.
     *
     * @param location The location to cache
     * @param ttlSeconds Time-to-live in seconds (default 30 days)
     */
    suspend fun put(
        location: GeocodedLocation,
        ttlSeconds: Long = 2592000
    )

    /**
     * Clear all expired cache entries.
     */
    suspend fun clearExpired()

    /**
     * Clear all cache entries.
     */
    suspend fun clear()

    /**
     * Get count of valid cached entries.
     */
    suspend fun count(): Long
}
