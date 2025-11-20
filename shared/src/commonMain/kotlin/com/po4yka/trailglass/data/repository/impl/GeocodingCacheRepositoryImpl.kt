package com.po4yka.trailglass.data.repository.impl

import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.repository.GeocodingCacheRepository
import com.po4yka.trailglass.domain.model.GeocodedLocation
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject
import kotlin.math.*

/**
 * SQLDelight implementation of GeocodingCacheRepository.
 * Uses spatial bounding box queries followed by Haversine distance calculation.
 */
@Inject
class GeocodingCacheRepositoryImpl(
    private val database: Database
) : GeocodingCacheRepository {

    private val queries = database.geocodingCacheQueries
    private val logger = logger()

    override suspend fun get(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): GeocodedLocation? = withContext(Dispatchers.IO) {
        logger.trace { "Looking up geocoding cache for ($latitude, $longitude) within ${radiusMeters}m" }
        val now = Clock.System.now().toEpochMilliseconds()

        // Calculate bounding box for efficient DB query
        val (minLat, maxLat, minLon, maxLon) = calculateBoundingBox(
            latitude, longitude, radiusMeters
        )

        // Get candidates within bounding box
        val candidates = queries.getNearby(
            minLat, maxLat, minLon, maxLon, now
        ).executeAsList()

        // Find closest entry within radius using Haversine distance
        val result = candidates
            .map { entry ->
                val distance = calculateDistance(
                    latitude, longitude,
                    entry.latitude, entry.longitude
                )
                entry to distance
            }
            .filter { (_, distance) -> distance <= radiusMeters }
            .minByOrNull { (_, distance) -> distance }
            ?.first
            ?.toGeocodedLocation()

        if (result != null) {
            logger.debug { "Cache HIT for ($latitude, $longitude): ${result.city ?: result.formattedAddress}" }
        } else {
            logger.trace { "Cache MISS for ($latitude, $longitude)" }
        }
        result
    }

    override suspend fun put(location: GeocodedLocation, ttlSeconds: Long) =
        withContext(Dispatchers.IO) {
            logger.debug { "Caching geocoded location: ${location.city ?: location.formattedAddress} at (${location.latitude}, ${location.longitude})" }
            val now = Clock.System.now().toEpochMilliseconds()
            val expiresAt = now + (ttlSeconds * 1000)

            val id = "${location.latitude},${location.longitude}"

            queries.insert(
                id = id,
                latitude = location.latitude,
                longitude = location.longitude,
                formatted_address = location.formattedAddress,
                city = location.city,
                state = location.state,
                country_code = location.countryCode,
                country_name = location.countryName,
                postal_code = location.postalCode,
                poi_name = location.poiName,
                street = location.street,
                street_number = location.streetNumber,
                cached_at = now,
                expires_at = expiresAt
            )
            logger.trace { "Successfully cached geocoded location at ($id)" }
        }

    override suspend fun clearExpired() = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.clearExpired(now)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        queries.clearAll()
    }

    override suspend fun count(): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.countCached(now).executeAsOne()
    }

    /**
     * Calculate bounding box coordinates for a given center point and radius.
     * Returns (minLat, maxLat, minLon, maxLon)
     */
    /**
     * Calculate bounding box coordinates for a given center point and radius.
     * Returns (minLat, maxLat, minLon, maxLon)
     */
    private fun calculateBoundingBox(
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double
    ): BoundingBox {
        val earthRadiusMeters = 6371000.0

        // Angular distance in radians
        val radDist = radiusMeters / earthRadiusMeters

        val minLat = centerLat - (radDist * 180.0 / PI)
        val maxLat = centerLat + (radDist * 180.0 / PI)

        // Adjust for longitude changes based on latitude
        val deltaLon = (asin(sin(radDist) / cos(centerLat * PI / 180.0))) * 180.0 / PI

        val minLon = centerLon - deltaLon
        val maxLon = centerLon + deltaLon

        return BoundingBox(minLat, maxLat, minLon, maxLon)
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     * Returns distance in meters.
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a = sin(dLat / 2).pow(2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }

    private fun com.po4yka.trailglass.db.Geocoding_cache.toGeocodedLocation() = GeocodedLocation(
        latitude = latitude,
        longitude = longitude,
        formattedAddress = formatted_address,
        city = city,
        state = state,
        countryCode = country_code,
        countryName = country_name,
        postalCode = postal_code,
        poiName = poi_name,
        street = street,
        streetNumber = street_number
    )

    private data class BoundingBox(
        val minLat: Double,
        val maxLat: Double,
        val minLon: Double,
        val maxLon: Double
    )
}
