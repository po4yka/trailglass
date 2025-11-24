package com.po4yka.trailglass.domain.usecase.region

import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

/**
 * Use case for checking if a coordinate is within any regions.
 * Returns all regions that contain the given point, sorted by distance from center.
 */
@Inject
class CheckIfInRegionUseCase(
    private val regionRepository: RegionRepository
) {
    private val logger = logger()

    /**
     * Check which regions contain the given coordinate.
     *
     * @param userId The user ID
     * @param lat The latitude to check
     * @param lon The longitude to check
     * @return List of regions that contain the point, sorted by distance from center
     */
    suspend fun execute(userId: String, lat: Double, lon: Double): List<Region> =
        try {
            val allRegions = regionRepository.getAllRegions(userId).first()

            // Filter regions that contain the point and sort by distance
            allRegions
                .filter { region -> region.contains(lat, lon) }
                .sortedBy { region -> region.distanceFrom(lat, lon) }
                .also { matchingRegions ->
                    if (matchingRegions.isNotEmpty()) {
                        logger.debug {
                            "Location ($lat, $lon) is in ${matchingRegions.size} region(s): ${matchingRegions.joinToString { it.name }}"
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to check regions for location ($lat, $lon)" }
            emptyList()
        }
}
