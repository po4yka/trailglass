package com.po4yka.trailglass.feature.performance

import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * Helper class for common performance traces.
 *
 * This class provides convenient methods for tracking performance of common operations
 * such as screen loads, database queries, network requests, and location processing.
 *
 * Example usage:
 * ```
 * performanceTracer.trackScreenLoad("timeline_screen") {
 *     // Load timeline screen data
 * }
 * ```
 */
@Inject
class PerformanceTracer(
    @PublishedApi internal val trackPerformanceTraceUseCase: TrackPerformanceTraceUseCase,
    @PublishedApi internal val recordPerformanceMetricUseCase: RecordPerformanceMetricUseCase
) {
    /**
     * Common trace names for standardized performance tracking.
     */
    object TraceNames {
        // Screen load traces
        const val TIMELINE_SCREEN_LOAD = "timeline_screen_load"
        const val MAP_SCREEN_LOAD = "map_screen_load"
        const val STATS_SCREEN_LOAD = "stats_screen_load"
        const val PLACES_SCREEN_LOAD = "places_screen_load"
        const val PHOTO_GALLERY_LOAD = "photo_gallery_load"
        const val SETTINGS_SCREEN_LOAD = "settings_screen_load"

        // Database operation traces
        const val DB_LOCATION_QUERY = "db_location_query"
        const val DB_PLACE_VISIT_QUERY = "db_place_visit_query"
        const val DB_ROUTE_SEGMENT_QUERY = "db_route_segment_query"
        const val DB_TRIP_QUERY = "db_trip_query"
        const val DB_PHOTO_QUERY = "db_photo_query"
        const val DB_INSERT_LOCATIONS = "db_insert_locations"

        // Location processing traces
        const val LOCATION_CLUSTERING = "location_clustering"
        const val ROUTE_DETECTION = "route_detection"
        const val TRIP_DETECTION = "trip_detection"
        const val GEOCODING_REQUEST = "geocoding_request"

        // Sync operation traces
        const val SYNC_LOCATIONS = "sync_locations"
        const val SYNC_PLACES = "sync_places"
        const val SYNC_PHOTOS = "sync_photos"
        const val SYNC_FULL = "sync_full"

        // Network request traces
        const val API_AUTH_REQUEST = "api_auth_request"
        const val API_UPLOAD_LOCATIONS = "api_upload_locations"
        const val API_DOWNLOAD_SYNC_DATA = "api_download_sync_data"
    }

    /**
     * Track screen load performance.
     *
     * @param screenName The name of the screen
     * @param block The code block to execute
     * @return The result of the block execution
     */
    inline fun <T> trackScreenLoad(
        screenName: String,
        block: () -> T
    ): T = trackPerformanceTraceUseCase.trace(screenName, block)

    /**
     * Track screen load performance (suspending version).
     *
     * @param screenName The name of the screen
     * @param block The suspending code block to execute
     * @return The result of the block execution
     */
    suspend inline fun <T> trackScreenLoadSuspend(
        screenName: String,
        crossinline block: suspend () -> T
    ): T = trackPerformanceTraceUseCase.traceSuspend(screenName, block)

    /**
     * Track database query performance.
     *
     * @param queryName The name of the query
     * @param block The code block to execute
     * @return The result of the block execution
     */
    suspend inline fun <T> trackDatabaseQuery(
        queryName: String,
        crossinline block: suspend () -> T
    ): T {
        val startTime = currentTimeMillis()
        trackPerformanceTraceUseCase.start(queryName)

        return try {
            val result = block()
            val duration = currentTimeMillis() - startTime
            recordPerformanceMetricUseCase.putMetric(queryName, "duration_ms", duration)
            result
        } finally {
            trackPerformanceTraceUseCase.stop(queryName)
        }
    }

    /**
     * Track location processing performance.
     *
     * @param operationName The name of the operation
     * @param pointsCount The number of location points being processed
     * @param block The code block to execute
     * @return The result of the block execution
     */
    suspend inline fun <T> trackLocationProcessing(
        operationName: String,
        pointsCount: Int,
        crossinline block: suspend () -> T
    ): T {
        val startTime = currentTimeMillis()
        trackPerformanceTraceUseCase.start(operationName)
        trackPerformanceTraceUseCase.putAttribute(operationName, "points_count", pointsCount.toString())

        return try {
            val result = block()
            val duration = currentTimeMillis() - startTime
            recordPerformanceMetricUseCase.recordLocationProcessing(
                traceName = operationName,
                processingTimeMs = duration,
                pointsProcessed = pointsCount
            )
            result
        } finally {
            trackPerformanceTraceUseCase.stop(operationName)
        }
    }

    /**
     * Track sync operation performance.
     *
     * @param syncType The type of sync operation
     * @param block The code block to execute
     * @return The result of the block execution
     */
    suspend inline fun <T> trackSyncOperation(
        syncType: String,
        crossinline block: suspend () -> Pair<T, Int>
    ): T {
        val startTime = currentTimeMillis()
        trackPerformanceTraceUseCase.start(syncType)

        return try {
            val (result, itemsSynced) = block()
            val duration = currentTimeMillis() - startTime
            recordPerformanceMetricUseCase.recordSyncOperation(
                traceName = syncType,
                syncDurationMs = duration,
                itemsSynced = itemsSynced
            )
            result
        } finally {
            trackPerformanceTraceUseCase.stop(syncType)
        }
    }

    /**
     * Track network request performance.
     *
     * @param requestName The name of the network request
     * @param block The code block to execute
     * @return The result of the block execution
     */
    suspend inline fun <T> trackNetworkRequest(
        requestName: String,
        crossinline block: suspend () -> Pair<T, Long>
    ): T {
        val startTime = currentTimeMillis()
        trackPerformanceTraceUseCase.start(requestName)

        return try {
            val (result, responseSize) = block()
            val duration = currentTimeMillis() - startTime
            recordPerformanceMetricUseCase.recordNetworkRequest(
                traceName = requestName,
                requestTimeMs = duration,
                responseSize = responseSize
            )
            result
        } finally {
            trackPerformanceTraceUseCase.stop(requestName)
        }
    }

    /**
     * Get current time in milliseconds.
     * Platform-agnostic implementation.
     */
    @PublishedApi
    internal fun currentTimeMillis(): Long =
        kotlinx.datetime.Clock.System
            .now()
            .toEpochMilliseconds()
}
