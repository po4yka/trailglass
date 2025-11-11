package com.po4yka.trailglass.feature.timeline

/**
 * Timeline zoom levels for different views.
 */
enum class TimelineZoomLevel {
    /**
     * Day view - shows detailed timeline for a single day.
     */
    DAY,

    /**
     * Week view - shows summary of visits and routes for a week.
     */
    WEEK,

    /**
     * Month view - shows overview of activity for a month.
     */
    MONTH,

    /**
     * Year view - shows high-level summary for the entire year.
     */
    YEAR;

    /**
     * Get display name for the zoom level.
     */
    val displayName: String
        get() = when (this) {
            DAY -> "Day"
            WEEK -> "Week"
            MONTH -> "Month"
            YEAR -> "Year"
        }
}
