package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.domain.model.*
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import me.tatarka.inject.annotations.Inject

/**
 * Categorizes places based on POI names, visit patterns, and temporal data.
 *
 * Uses heuristics like:
 * - POI name keywords (e.g., "restaurant", "gym", "airport")
 * - Visit frequency and duration
 * - Time of day patterns (work hours, evenings, weekends)
 */
@Inject
class PlaceCategorizer {

    /**
     * Categorize a place visit based on available information.
     *
     * @param visit The place visit to categorize
     * @param visitHistory Previous visits to the same place (for pattern analysis)
     * @return Pair of category and confidence level
     */
    fun categorize(
        visit: PlaceVisit,
        visitHistory: List<PlaceVisit> = emptyList()
    ): Pair<PlaceCategory, CategoryConfidence> {

        // Try POI-based categorization first (highest confidence)
        visit.poiName?.let { poiName ->
            categorizeBPOI(poiName)?.let { category ->
                return category to CategoryConfidence.HIGH
            }
        }

        // Try pattern-based categorization if we have visit history
        if (visitHistory.isNotEmpty()) {
            categorizeByPattern(visit, visitHistory)?.let { categoryWithConfidence ->
                return categoryWithConfidence
            }
        }

        // Default to OTHER with low confidence
        return PlaceCategory.OTHER to CategoryConfidence.LOW
    }

    /**
     * Categorize based on POI name keywords.
     */
    private fun categorizeBPOI(poiName: String): PlaceCategory? {
        val normalized = poiName.lowercase()

        return when {
            // Food & Dining
            normalized.contains("restaurant") ||
            normalized.contains("cafe") ||
            normalized.contains("coffee") ||
            normalized.contains("diner") ||
            normalized.contains("pizzeria") ||
            normalized.contains("burger") ||
            normalized.contains("sushi") ||
            normalized.contains("bakery") ||
            normalized.contains("bar") ||
            normalized.contains("pub") ||
            normalized.contains("bistro") -> PlaceCategory.FOOD

            // Shopping
            normalized.contains("mall") ||
            normalized.contains("shop") ||
            normalized.contains("store") ||
            normalized.contains("market") ||
            normalized.contains("boutique") ||
            normalized.contains("supermarket") ||
            normalized.contains("grocery") -> PlaceCategory.SHOPPING

            // Fitness
            normalized.contains("gym") ||
            normalized.contains("fitness") ||
            normalized.contains("yoga") ||
            normalized.contains("sports") ||
            normalized.contains("pool") ||
            normalized.contains("stadium") ||
            normalized.contains("arena") -> PlaceCategory.FITNESS

            // Entertainment
            normalized.contains("cinema") ||
            normalized.contains("theater") ||
            normalized.contains("theatre") ||
            normalized.contains("museum") ||
            normalized.contains("gallery") ||
            normalized.contains("concert") ||
            normalized.contains("club") ||
            normalized.contains("arcade") -> PlaceCategory.ENTERTAINMENT

            // Travel
            normalized.contains("airport") ||
            normalized.contains("station") ||
            normalized.contains("terminal") ||
            normalized.contains("hotel") ||
            normalized.contains("motel") ||
            normalized.contains("hostel") ||
            normalized.contains("resort") -> PlaceCategory.TRAVEL

            // Healthcare
            normalized.contains("hospital") ||
            normalized.contains("clinic") ||
            normalized.contains("doctor") ||
            normalized.contains("dentist") ||
            normalized.contains("pharmacy") ||
            normalized.contains("medical") -> PlaceCategory.HEALTHCARE

            // Education
            normalized.contains("school") ||
            normalized.contains("university") ||
            normalized.contains("college") ||
            normalized.contains("library") ||
            normalized.contains("academy") -> PlaceCategory.EDUCATION

            // Religious
            normalized.contains("church") ||
            normalized.contains("mosque") ||
            normalized.contains("temple") ||
            normalized.contains("synagogue") ||
            normalized.contains("chapel") -> PlaceCategory.RELIGIOUS

            // Outdoor
            normalized.contains("park") ||
            normalized.contains("garden") ||
            normalized.contains("beach") ||
            normalized.contains("trail") ||
            normalized.contains("nature") ||
            normalized.contains("forest") -> PlaceCategory.OUTDOOR

            // Service
            normalized.contains("bank") ||
            normalized.contains("atm") ||
            normalized.contains("post office") ||
            normalized.contains("salon") ||
            normalized.contains("barber") ||
            normalized.contains("laundry") ||
            normalized.contains("gas station") -> PlaceCategory.SERVICE

            else -> null
        }
    }

    /**
     * Categorize based on visit patterns and temporal data.
     */
    private fun categorizeByPattern(
        visit: PlaceVisit,
        visitHistory: List<PlaceVisit>
    ): Pair<PlaceCategory, CategoryConfidence>? {

        val totalVisits = visitHistory.size + 1
        val totalDuration = visitHistory.sumOf { it.duration.inWholeSeconds } + visit.duration.inWholeSeconds
        val avgDuration = Duration.parse("${totalDuration / totalVisits}s")

        // Analyze time of day patterns
        val timeOfDayPattern = analyzeTimeOfDayPattern(visitHistory + visit)

        // Very frequent visits with long durations -> likely HOME
        if (totalVisits >= 10 && avgDuration > 4.hours) {
            return PlaceCategory.HOME to CategoryConfidence.MEDIUM
        }

        // Weekday daytime pattern with medium duration -> likely WORK
        if (timeOfDayPattern.isWeekdayDaytime && totalVisits >= 5 && avgDuration > 2.hours) {
            return PlaceCategory.WORK to CategoryConfidence.MEDIUM
        }

        // Evening/weekend pattern -> likely SOCIAL or ENTERTAINMENT
        if (timeOfDayPattern.isEveningOrWeekend && totalVisits >= 3) {
            return if (avgDuration > 2.hours) {
                PlaceCategory.SOCIAL to CategoryConfidence.LOW
            } else {
                PlaceCategory.ENTERTAINMENT to CategoryConfidence.LOW
            }
        }

        return null
    }

    /**
     * Analyze temporal patterns in visit history.
     */
    private fun analyzeTimeOfDayPattern(visits: List<PlaceVisit>): TimeOfDayPattern {
        var weekdayDaytimeCount = 0
        var eveningCount = 0
        var weekendCount = 0

        visits.forEach { visit ->
            val startDateTime = visit.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
            val isWeekday = startDateTime.dayOfWeek != DayOfWeek.SATURDAY &&
                           startDateTime.dayOfWeek != DayOfWeek.SUNDAY
            val hour = startDateTime.hour

            when {
                !isWeekday -> weekendCount++
                hour in 9..17 -> weekdayDaytimeCount++
                hour >= 18 -> eveningCount++
            }
        }

        val total = visits.size
        return TimeOfDayPattern(
            isWeekdayDaytime = weekdayDaytimeCount.toDouble() / total > 0.6,
            isEveningOrWeekend = (eveningCount + weekendCount).toDouble() / total > 0.6
        )
    }

    /**
     * Determine place significance based on visit frequency and recency.
     */
    fun determineSignificance(
        visitCount: Int,
        totalDuration: Duration,
        lastVisitTime: Instant
    ): PlaceSignificance {
        val now = Clock.System.now()
        val daysSinceLastVisit = (now - lastVisitTime).inWholeDays

        return when {
            // Very frequent and recent -> PRIMARY
            visitCount >= 20 && daysSinceLastVisit <= 7 -> PlaceSignificance.PRIMARY

            // Frequent -> FREQUENT
            visitCount >= 10 && daysSinceLastVisit <= 30 -> PlaceSignificance.FREQUENT

            // Regular but not super recent -> OCCASIONAL
            visitCount >= 3 && daysSinceLastVisit <= 90 -> PlaceSignificance.OCCASIONAL

            // Everything else -> RARE
            else -> PlaceSignificance.RARE
        }
    }

    private data class TimeOfDayPattern(
        val isWeekdayDaytime: Boolean,
        val isEveningOrWeekend: Boolean
    )
}
