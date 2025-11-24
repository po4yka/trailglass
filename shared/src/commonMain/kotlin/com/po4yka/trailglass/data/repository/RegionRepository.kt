package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Repository for managing user-defined regions (geofences).
 */
interface RegionRepository {
    /**
     * Get all regions for a user as a Flow.
     */
    fun getAllRegions(userId: String): Flow<List<Region>>

    /**
     * Get a specific region by ID.
     */
    suspend fun getRegionById(id: String): Region?

    /**
     * Get all active regions (notifications enabled) for a user.
     */
    suspend fun getActiveRegions(userId: String): List<Region>

    /**
     * Insert a new region.
     */
    suspend fun insertRegion(region: Region)

    /**
     * Update an existing region.
     */
    suspend fun updateRegion(region: Region)

    /**
     * Delete a region.
     */
    suspend fun deleteRegion(id: String)

    /**
     * Update region enter statistics.
     * @param regionId The region ID
     * @param timestamp The timestamp of the enter event
     */
    suspend fun updateEnterStats(regionId: String, timestamp: Instant)

    /**
     * Update region exit statistics.
     * @param regionId The region ID
     * @param timestamp The timestamp of the exit event
     */
    suspend fun updateExitStats(regionId: String, timestamp: Instant)
}
