package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.RouteSegment
import kotlinx.datetime.Instant

/**
 * Repository for RouteSegment data.
 */
interface RouteSegmentRepository {
    /**
     * Insert a route segment with its location samples and path.
     */
    suspend fun insertRouteSegment(segment: RouteSegment)

    /**
     * Get a route segment by ID (with samples and path loaded).
     */
    suspend fun getRouteSegmentById(segmentId: String): RouteSegment?

    /**
     * Get route segments within a time range.
     */
    suspend fun getRouteSegmentsInRange(
        startTime: Instant,
        endTime: Instant
    ): List<RouteSegment>

    /**
     * Get route segments for a specific trip.
     */
    suspend fun getRouteSegmentsForTrip(
        fromVisitId: String?,
        toVisitId: String?
    ): List<RouteSegment>

    /**
     * Delete a route segment.
     */
    suspend fun deleteRouteSegment(segmentId: String)

    /**
     * Get the total number of route segments.
     */
    suspend fun getRouteSegmentCount(): Long
}
