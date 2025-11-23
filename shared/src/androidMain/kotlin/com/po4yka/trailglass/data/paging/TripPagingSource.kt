package com.po4yka.trailglass.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.po4yka.trailglass.db.TrailGlassDatabase
import com.po4yka.trailglass.domain.model.Trip
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * PagingSource for loading trips with pagination.
 *
 * @param database SQLDelight database instance
 * @param userId User ID to filter trips
 */
class TripPagingSource(
    private val database: TrailGlassDatabase,
    private val userId: String
) : PagingSource<Int, Trip>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Trip> =
        try {
            val page = params.key ?: 0
            val offset = page * params.loadSize
            val limit = params.loadSize.toLong()

            logger.debug { "Loading trips: page=$page, offset=$offset, limit=$limit" }

            val trips =
                database.tripsQueries
                    .getTripsWithPagination(userId, limit, offset.toLong())
                    .executeAsList()
                    .map { it.toTrip() }

            logger.debug { "Loaded ${trips.size} trips" }

            LoadResult.Page(
                data = trips,
                prevKey = if (page == 0) null else page - 1,
                nextKey =
                    if (trips.isEmpty() || trips.size < params.loadSize) {
                        null
                    } else {
                        page + 1
                    }
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to load trips" }
            LoadResult.Error(e)
        }

    override fun getRefreshKey(state: PagingState<Int, Trip>): Int? {
        // Return the page closest to the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

/** Extension function to convert database entity to domain model. */
private fun com.po4yka.trailglass.db.Trips.toTrip(): Trip =
    Trip(
        id = id,
        name = name,
        startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(start_time),
        endTime = end_time?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
        primaryCountry = primary_country,
        isOngoing = is_ongoing != 0L,
        userId = user_id,
        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(created_at),
        updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updated_at)
    )
