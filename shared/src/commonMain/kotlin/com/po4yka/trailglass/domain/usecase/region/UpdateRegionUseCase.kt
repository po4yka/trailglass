package com.po4yka.trailglass.domain.usecase.region

import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * Use case for updating an existing region.
 * Validates region data before updating.
 */
@Inject
class UpdateRegionUseCase(
    private val regionRepository: RegionRepository
) {
    private val logger = logger()

    /**
     * Update an existing region.
     *
     * @param region The region to update
     * @return Result containing the updated region or an error
     */
    suspend fun execute(region: Region): Result<Region> {
        return try {
            // Validate region data
            if (region.name.isBlank()) {
                return Result.failure(IllegalArgumentException("Region name cannot be empty"))
            }

            if (region.radiusMeters <= 0) {
                return Result.failure(IllegalArgumentException("Region radius must be greater than 0"))
            }

            if (region.radiusMeters < Region.MIN_RADIUS_METERS || region.radiusMeters > Region.MAX_RADIUS_METERS) {
                return Result.failure(
                    IllegalArgumentException(
                        "Region radius must be between ${Region.MIN_RADIUS_METERS} and ${Region.MAX_RADIUS_METERS} meters"
                    )
                )
            }

            if (region.latitude < -90.0 || region.latitude > 90.0) {
                return Result.failure(IllegalArgumentException("Latitude must be between -90 and 90"))
            }

            if (region.longitude < -180.0 || region.longitude > 180.0) {
                return Result.failure(IllegalArgumentException("Longitude must be between -180 and 180"))
            }

            // Verify region exists
            val existingRegion = regionRepository.getRegionById(region.id)
                ?: return Result.failure(IllegalArgumentException("Region not found: ${region.id}"))

            // Update the region with new updatedAt timestamp
            val updatedRegion = region.copy(updatedAt = Clock.System.now())
            regionRepository.updateRegion(updatedRegion)

            logger.info { "Updated region: ${updatedRegion.name} (${updatedRegion.id})" }

            Result.success(updatedRegion)
        } catch (e: Exception) {
            logger.error(e) { "Failed to update region: ${region.id}" }
            Result.failure(e)
        }
    }
}
