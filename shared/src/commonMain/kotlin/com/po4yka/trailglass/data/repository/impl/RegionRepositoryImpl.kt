package com.po4yka.trailglass.data.repository.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.resultOf
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * SQLDelight implementation of RegionRepository.
 */
@Inject
class RegionRepositoryImpl(
    private val database: Database
) : RegionRepository {
    private val logger = logger()
    private val queries = database.regionsQueries

    override fun getAllRegions(userId: String): Flow<List<Region>> =
        queries
            .selectAllForUser(userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { regions -> regions.map { it.toRegion() } }

    override suspend fun getRegionById(id: String): Region? =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting region by ID: $id" }
            try {
                queries
                    .selectById(id)
                    .executeAsOneOrNull()
                    ?.toRegion()
                    .also { region ->
                        logger.trace { if (region != null) "Found region $id" else "Region $id not found" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get region $id" }
                throw e
            }
        }

    override suspend fun getActiveRegions(userId: String): List<Region> =
        withContext(Dispatchers.IO) {
            logger.debug { "Getting active regions for user $userId" }
            try {
                queries
                    .selectActiveForUser(userId)
                    .executeAsList()
                    .map { it.toRegion() }
                    .also { regions ->
                        logger.debug { "Found ${regions.size} active regions" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get active regions for user $userId" }
                throw e
            }
        }

    override suspend fun insertRegion(region: Region): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.info { "Inserting region: ${region.id} - ${region.name}" }
                queries.insert(
                    id = region.id,
                    user_id = region.userId,
                    name = region.name,
                    description = region.description,
                    latitude = region.latitude,
                    longitude = region.longitude,
                    radius_meters = region.radiusMeters.toLong(),
                    notifications_enabled = if (region.notificationsEnabled) 1L else 0L,
                    created_at = region.createdAt.toEpochMilliseconds(),
                    updated_at = region.updatedAt.toEpochMilliseconds(),
                    enter_count = region.enterCount.toLong(),
                    last_enter_time = region.lastEnterTime?.toEpochMilliseconds(),
                    last_exit_time = region.lastExitTime?.toEpochMilliseconds()
                )
                logger.debug { "Successfully inserted region ${region.id}" }
            }
        }

    override suspend fun updateRegion(region: Region): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Updating region: ${region.id}" }
                queries.update(
                    name = region.name,
                    description = region.description,
                    latitude = region.latitude,
                    longitude = region.longitude,
                    radius_meters = region.radiusMeters.toLong(),
                    notifications_enabled = if (region.notificationsEnabled) 1L else 0L,
                    updated_at = region.updatedAt.toEpochMilliseconds(),
                    id = region.id
                )
                logger.debug { "Successfully updated region ${region.id}" }
            }
        }

    override suspend fun deleteRegion(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Deleting region $id" }
                queries.delete(id)
                logger.info { "Region $id deleted" }
            }
        }

    override suspend fun updateEnterStats(regionId: String, timestamp: Instant) =
        withContext(Dispatchers.IO) {
            logger.debug { "Updating enter stats for region $regionId" }
            try {
                queries.updateEnterStats(
                    last_enter_time = timestamp.toEpochMilliseconds(),
                    updated_at = Clock.System.now().toEpochMilliseconds(),
                    id = regionId
                )
                logger.debug { "Successfully updated enter stats for region $regionId" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update enter stats for region $regionId" }
                throw e
            }
        }

    override suspend fun updateExitStats(regionId: String, timestamp: Instant) =
        withContext(Dispatchers.IO) {
            logger.debug { "Updating exit stats for region $regionId at $timestamp" }
            try {
                queries.updateExitStats(
                    last_exit_time = timestamp.toEpochMilliseconds(),
                    updated_at = Clock.System.now().toEpochMilliseconds(),
                    id = regionId
                )
                logger.debug { "Successfully updated exit stats for region $regionId" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update exit stats for region $regionId" }
                throw e
            }
        }

    /**
     * Map database row to Region domain object.
     */
    private fun com.po4yka.trailglass.db.Regions.toRegion(): Region =
        Region(
            id = id,
            userId = user_id,
            name = name,
            description = description,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radius_meters.toInt(),
            notificationsEnabled = notifications_enabled != 0L,
            createdAt = Instant.fromEpochMilliseconds(created_at),
            updatedAt = Instant.fromEpochMilliseconds(updated_at),
            enterCount = enter_count.toInt(),
            lastEnterTime = last_enter_time?.let { Instant.fromEpochMilliseconds(it) },
            lastExitTime = last_exit_time?.let { Instant.fromEpochMilliseconds(it) }
        )
}
