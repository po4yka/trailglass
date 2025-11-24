package com.po4yka.trailglass.domain.usecase.region

import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for deleting a region.
 */
@Inject
class DeleteRegionUseCase(
    private val regionRepository: RegionRepository
) {
    private val logger = logger()

    /**
     * Delete a region by ID.
     *
     * @param regionId The region ID to delete
     * @return Result indicating success or failure
     */
    suspend fun execute(regionId: String): Result<Unit> =
        try {
            // Verify region exists before deleting
            val region = regionRepository.getRegionById(regionId)
                ?: return Result.failure(IllegalArgumentException("Region not found: $regionId"))

            regionRepository.deleteRegion(regionId)
            logger.info { "Deleted region: ${region.name} ($regionId)" }

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete region: $regionId" }
            Result.failure(e)
        }
}
