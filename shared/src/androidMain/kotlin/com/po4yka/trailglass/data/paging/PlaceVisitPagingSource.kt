package com.po4yka.trailglass.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.po4yka.trailglass.db.TrailGlassDatabase
import com.po4yka.trailglass.domain.model.PlaceVisit
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * PagingSource for loading place visits with pagination.
 *
 * @param database SQLDelight database instance
 * @param userId User ID to filter visits
 */
class PlaceVisitPagingSource(
    private val database: TrailGlassDatabase,
    private val userId: String
) : PagingSource<Int, PlaceVisit>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlaceVisit> {
        return try {
            val page = params.key ?: 0
            val offset = page * params.loadSize
            val limit = params.loadSize.toLong()

            logger.debug { "Loading place visits: page=$page, offset=$offset, limit=$limit" }

            val visits = database.placeVisitsQueries
                .getVisitsByUser(userId, limit, offset.toLong())
                .executeAsList()
                .map { it.toPlaceVisit() }

            logger.debug { "Loaded ${visits.size} place visits" }

            LoadResult.Page(
                data = visits,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (visits.isEmpty() || visits.size < params.loadSize) {
                    null
                } else {
                    page + 1
                }
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to load place visits" }
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PlaceVisit>): Int? {
        // Return the page closest to the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

/**
 * Extension function to convert database entity to domain model.
 */
private fun com.po4yka.trailglass.db.Place_visits.toPlaceVisit(): PlaceVisit {
    return PlaceVisit(
        id = id,
        startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(start_time),
        endTime = kotlinx.datetime.Instant.fromEpochMilliseconds(end_time),
        centerLatitude = center_latitude,
        centerLongitude = center_longitude,
        approximateAddress = approximate_address,
        poiName = poi_name,
        city = city,
        countryCode = country_code,
        category = com.po4yka.trailglass.domain.model.PlaceCategory.valueOf(category),
        categoryConfidence = com.po4yka.trailglass.domain.model.CategoryConfidence.valueOf(category_confidence),
        significance = com.po4yka.trailglass.domain.model.PlaceSignificance.valueOf(significance),
        userLabel = user_label,
        userNotes = user_notes,
        isFavorite = is_favorite != 0L,
        frequentPlaceId = frequent_place_id,
        userId = user_id,
        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(created_at),
        updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updated_at)
    )
}
