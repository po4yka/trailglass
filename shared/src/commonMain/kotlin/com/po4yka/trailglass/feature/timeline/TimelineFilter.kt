package com.po4yka.trailglass.feature.timeline

import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.TransportType
import kotlinx.datetime.Instant

/**
 * Filter criteria for timeline items.
 */
data class TimelineFilter(
    val transportTypes: Set<TransportType> = emptySet(),
    val placeCategories: Set<PlaceCategory> = emptySet(),
    val countries: Set<String> = emptySet(),
    val cities: Set<String> = emptySet(),
    val searchQuery: String? = null,
    val dateRange: DateRange? = null,
    val minDuration: kotlin.time.Duration? = null,
    val maxDuration: kotlin.time.Duration? = null,
    val showOnlyFavorites: Boolean = false
) {
    /**
     * Check if any filters are active.
     */
    val isActive: Boolean
        get() = transportTypes.isNotEmpty() ||
                placeCategories.isNotEmpty() ||
                countries.isNotEmpty() ||
                cities.isNotEmpty() ||
                searchQuery != null ||
                dateRange != null ||
                minDuration != null ||
                maxDuration != null ||
                showOnlyFavorites

    /**
     * Get count of active filters.
     */
    val activeFilterCount: Int
        get() = listOf(
            transportTypes.isNotEmpty(),
            placeCategories.isNotEmpty(),
            countries.isNotEmpty(),
            cities.isNotEmpty(),
            searchQuery != null,
            dateRange != null,
            minDuration != null,
            maxDuration != null,
            showOnlyFavorites
        ).count { it }
}

/**
 * Date range for filtering.
 */
data class DateRange(
    val startTime: Instant,
    val endTime: Instant
)
