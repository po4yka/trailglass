package com.po4yka.trailglass.location.geocoding

import com.po4yka.trailglass.domain.model.GeocodedLocation
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * In-memory cache for geocoding results.
 * Uses spatial proximity to return cached results for nearby coordinates.
 */
class GeocodingCache(
    private val proximityThresholdMeters: Double = 100.0,
    private val cacheDuration: Duration = 30.days
) {
    private data class CacheEntry(
        val location: GeocodedLocation,
        val cachedAt: Instant
    )

    private val cache = mutableMapOf<String, CacheEntry>()

    /**
     * Get a cached geocoded location if available within proximity threshold.
     *
     * @param latitude The latitude to look up
     * @param longitude The longitude to look up
     * @return Cached GeocodedLocation if found within proximity, null otherwise
     */
    fun get(latitude: Double, longitude: Double): GeocodedLocation? {
        val now = Clock.System.now()

        // Find nearest cached entry within threshold
        return cache.values
            .filter { (now - it.cachedAt) < cacheDuration }
            .minByOrNull { entry ->
                calculateDistance(
                    latitude, longitude,
                    entry.location.latitude, entry.location.longitude
                )
            }
            ?.takeIf { entry ->
                calculateDistance(
                    latitude, longitude,
                    entry.location.latitude, entry.location.longitude
                ) < proximityThresholdMeters
            }
            ?.location
    }

    /**
     * Store a geocoded location in the cache.
     *
     * @param location The geocoded location to cache
     */
    fun put(location: GeocodedLocation) {
        val key = "${location.latitude},${location.longitude}"
        cache[key] = CacheEntry(location, Clock.System.now())
    }

    /**
     * Clear expired entries from the cache.
     */
    fun clearExpired() {
        val now = Clock.System.now()
        cache.entries.removeIf { (_, entry) ->
            (now - entry.cachedAt) >= cacheDuration
        }
    }

    /**
     * Clear all cache entries.
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     *
     * @return Distance in meters
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
