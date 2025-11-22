package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.data.repository.GeocodingCacheRepository
import com.po4yka.trailglass.domain.model.GeocodedLocation
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Database-backed geocoding cache that persists across app restarts.
 * Replaces the in-memory GeocodingCache with persistent storage.
 */
@Inject
class DatabaseGeocodingCache(
    private val repository: GeocodingCacheRepository,
    private val proximityThresholdMeters: Double = 100.0,
    private val cacheDuration: Duration = 30.days
) {
    /**
     * Get a cached geocoded location if available within proximity threshold.
     *
     * @param latitude The latitude to look up
     * @param longitude The longitude to look up
     * @return Cached GeocodedLocation if found within proximity, null otherwise
     */
    suspend fun get(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? = repository.get(latitude, longitude, proximityThresholdMeters)

    /**
     * Store a geocoded location in the cache.
     *
     * @param location The geocoded location to cache
     */
    suspend fun put(location: GeocodedLocation) {
        val ttlSeconds = cacheDuration.inWholeSeconds
        repository.put(location, ttlSeconds)
    }

    /**
     * Clear expired entries from the cache.
     */
    suspend fun clearExpired() {
        repository.clearExpired()
    }

    /**
     * Clear all cache entries.
     */
    suspend fun clear() {
        repository.clear()
    }

    /**
     * Get count of valid cached entries.
     */
    suspend fun count(): Long = repository.count()
}
