package com.po4yka.trailglass.domain.usecase.region

import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * Use case for creating a new region.
 * Validates region data before creating.
 */
@Inject
class CreateRegionUseCase(
    private val regionRepository: RegionRepository
) {
    private val logger = logger()

    /**
     * Create a new region.
     *
     * @param id Unique region ID
     * @param userId User ID
     * @param name Region name (must not be empty)
     * @param description Optional region description
     * @param lat Center latitude
     * @param lon Center longitude
     * @param radius Radius in meters (must be > 0)
     * @param notificationsEnabled Whether notifications are enabled
     * @return Result containing the created region or an error
     */
    suspend fun execute(
        id: String,
        userId: String,
        name: String,
        description: String?,
        lat: Double,
        lon: Double,
        radius: Int,
        notificationsEnabled: Boolean
    ): Result<Region> {
        return try {
            // Validate region data
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Region name cannot be empty"))
            }

            if (radius <= 0) {
                return Result.failure(IllegalArgumentException("Region radius must be greater than 0"))
            }

            if (radius < Region.MIN_RADIUS_METERS || radius > Region.MAX_RADIUS_METERS) {
                return Result.failure(
                    IllegalArgumentException(
                        "Region radius must be between ${Region.MIN_RADIUS_METERS} and ${Region.MAX_RADIUS_METERS} meters"
                    )
                )
            }

            if (lat < -90.0 || lat > 90.0) {
                return Result.failure(IllegalArgumentException("Latitude must be between -90 and 90"))
            }

            if (lon < -180.0 || lon > 180.0) {
                return Result.failure(IllegalArgumentException("Longitude must be between -180 and 180"))
            }

            val now = Clock.System.now()
            val region = Region(
                id = id,
                userId = userId,
                name = name.trim(),
                description = description?.trim(),
                latitude = lat,
                longitude = lon,
                radiusMeters = radius,
                notificationsEnabled = notificationsEnabled,
                createdAt = now,
                updatedAt = now
            )

            when (val result = regionRepository.insertRegion(region)) {
                is com.po4yka.trailglass.domain.error.Result.Success -> {
                    logger.info { "Created region: ${region.name} (${region.id})" }
                    Result.success(region)
                }
                is com.po4yka.trailglass.domain.error.Result.Error -> {
                    logger.error { "Failed to insert region: ${result.error.getUserFriendlyMessage()}" }
                    return Result.failure(Exception(result.error.getUserFriendlyMessage()))
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to create region" }
            Result.failure(e)
        }
    }
}
