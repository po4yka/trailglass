package com.po4yka.trailglass.feature.stats.models

import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.TransportType
import kotlinx.datetime.DayOfWeek

/** Distance statistics broken down by transport type. */
data class DistanceStatistics(
    val totalDistanceMeters: Double,
    val byTransportType: Map<TransportType, Double>, // meters per transport type
    val totalDuration: kotlin.time.Duration,
    val averageSpeed: Double // km/h
) {
    val totalDistanceKm: Double get() = totalDistanceMeters / 1000.0

    fun distanceKmByType(type: TransportType): Double = (byTransportType[type] ?: 0.0) / 1000.0

    val mostUsedTransportType: TransportType? =
        byTransportType.maxByOrNull { it.value }?.key
}

/** Place visit statistics and patterns. */
data class PlaceStatistics(
    val totalPlaces: Int,
    val totalVisits: Int,
    val mostVisitedPlaces: List<PlaceVisitCount>,
    val visitsByCategory: Map<PlaceCategory, Int>,
    val averageVisitDuration: kotlin.time.Duration,
    val longestVisit: VisitRecord?,
    val shortestVisit: VisitRecord?
) {
    val topCategory: PlaceCategory? = visitsByCategory.maxByOrNull { it.value }?.key
    val averageVisitsPerPlace: Double = if (totalPlaces > 0) totalVisits.toDouble() / totalPlaces else 0.0
}

data class PlaceVisitCount(
    val placeId: String?,
    val placeName: String,
    val visitCount: Int,
    val totalDuration: kotlin.time.Duration,
    val category: PlaceCategory,
    val city: String?,
    val country: String?
)

data class VisitRecord(
    val placeName: String,
    val duration: kotlin.time.Duration,
    val category: PlaceCategory
)

/** Travel patterns based on time analysis. */
data class TravelPatterns(
    val hourlyActivity: Map<Int, ActivityCount>, // hour (0-23) to activity count
    val weekdayActivity: Map<DayOfWeek, ActivityCount>, // day of week to activity count
    val peakTravelHour: Int?,
    val peakTravelDay: DayOfWeek?,
    val weekdayVsWeekend: WeekdaySplit
) {
    val mostActiveHour: Int? = hourlyActivity.maxByOrNull { it.value.totalEvents }?.key
    val mostActiveDay: DayOfWeek? = weekdayActivity.maxByOrNull { it.value.totalEvents }?.key
}

data class ActivityCount(
    val totalEvents: Int,
    val visits: Int,
    val routes: Int,
    val totalDistance: Double // meters
)

data class WeekdaySplit(
    val weekdayTrips: Int,
    val weekendTrips: Int,
    val weekdayDistance: Double, // meters
    val weekendDistance: Double // meters
) {
    val weekdayPercentage: Double =
        if (weekdayTrips + weekendTrips > 0) {
            weekdayTrips.toDouble() / (weekdayTrips + weekendTrips) * 100
        } else {
            0.0
        }

    val weekendPercentage: Double = 100.0 - weekdayPercentage
}

/** Geographic statistics. */
data class GeographicStatistics(
    val countries: Set<String>,
    val cities: Set<String>,
    val topCountries: List<CountryStats>,
    val topCities: List<CityStats>,
    val furthestLocation: LocationRecord?,
    val homeBase: LocationRecord?
)

data class CountryStats(
    val countryCode: String,
    val visitCount: Int,
    val totalDuration: kotlin.time.Duration,
    val cities: Set<String>
)

data class CityStats(
    val city: String,
    val countryCode: String?,
    val visitCount: Int,
    val totalDuration: kotlin.time.Duration
)

data class LocationRecord(
    val name: String,
    val city: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double
)

/** Comprehensive statistics combining all metrics. */
data class ComprehensiveStatistics(
    val period: String,
    val distanceStats: DistanceStatistics,
    val placeStats: PlaceStatistics,
    val travelPatterns: TravelPatterns,
    val geographicStats: GeographicStatistics,
    val totalTrips: Int,
    val activeDays: Int,
    val totalTimeTracking: kotlin.time.Duration
)
