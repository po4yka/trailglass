package com.po4yka.trailglass.domain.usecase.region

import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

/**
 * Use case for getting all regions for a user.
 * Returns regions sorted by name.
 */
@Inject
class GetAllRegionsUseCase(
    private val regionRepository: RegionRepository
) {
    /**
     * Get all regions for a user, sorted by name.
     *
     * @param userId The user ID
     * @return Flow of regions sorted by name
     */
    fun execute(userId: String): Flow<List<Region>> =
        regionRepository.getAllRegions(userId).map { regions ->
            regions.sortedBy { it.name.lowercase() }
        }
}
