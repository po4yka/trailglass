package com.po4yka.trailglass.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.paging.PlaceVisitPagingSource
import com.po4yka.trailglass.data.paging.TripPagingSource
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.Trip

/**
 * Android-specific extension functions for Paging 3 support.
 * These extensions add pagination capabilities to repositories on Android.
 */

/**
 * Get a Pager for paginated place visits (Android only).
 *
 * @param userId User ID to filter visits
 * @param database Database instance (needed to access the raw database)
 * @return Pager configured for place visits
 */
fun PlaceVisitRepository.getVisitsPager(
    userId: String,
    database: Database
): Pager<Int, PlaceVisit> =
    Pager(
        config =
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
        pagingSourceFactory = {
            PlaceVisitPagingSource(
                database = database.database,
                userId = userId
            )
        }
    )

/**
 * Get a Pager for paginated trips (Android only).
 *
 * @param userId User ID to filter trips
 * @param database Database instance (needed to access the raw database)
 * @return Pager configured for trips
 */
fun TripRepository.getTripsPager(
    userId: String,
    database: Database
): Pager<Int, Trip> =
    Pager(
        config =
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
        pagingSourceFactory = {
            TripPagingSource(
                database = database.database,
                userId = userId
            )
        }
    )
