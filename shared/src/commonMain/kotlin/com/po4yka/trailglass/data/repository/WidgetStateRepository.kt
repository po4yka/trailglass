package com.po4yka.trailglass.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing widget state and data.
 *
 * Provides data that widgets need to display current travel stats.
 */
interface WidgetStateRepository {
    /**
     * Get today's travel statistics for widget display.
     *
     * @return Flow of today's widget data
     */
    fun getTodayStats(): Flow<WidgetStats>

    /**
     * Get today's statistics as a one-time value.
     *
     * Useful for widget updates without observing changes.
     */
    suspend fun getTodayStatsSnapshot(): WidgetStats

    /**
     * Check if location tracking is currently active.
     *
     * @return true if tracking, false otherwise
     */
    suspend fun isTrackingActive(): Boolean
}

/**
 * Data class containing stats for widget display.
 */
data class WidgetStats(
    val distanceKm: Double,
    val placesVisited: Int,
    val isTracking: Boolean
)
