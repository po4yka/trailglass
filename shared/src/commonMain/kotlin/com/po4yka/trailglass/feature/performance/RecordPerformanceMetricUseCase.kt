package com.po4yka.trailglass.feature.performance

import com.po4yka.trailglass.domain.service.PerformanceMonitoringService
import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * Use case for recording performance metrics.
 *
 * This use case provides a convenient way to record custom metrics associated with traces
 * for capturing performance data.
 *
 * Example usage:
 * ```
 * recordPerformanceMetricUseCase.putMetric(
 *     traceName = "location_processing",
 *     metricName = "points_processed",
 *     value = 150
 * )
 * ```
 */
@Inject
class RecordPerformanceMetricUseCase(
    private val performanceMonitoringService: PerformanceMonitoringService
) {
    /**
     * Record a metric value for a trace.
     *
     * @param traceName The name of the trace
     * @param metricName The name of the metric
     * @param value The metric value
     */
    fun putMetric(
        traceName: String,
        metricName: String,
        value: Long
    ) {
        logger.debug { "Recording metric '$metricName' = $value for trace '$traceName'" }
        performanceMonitoringService.putMetric(traceName, metricName, value)
    }

    /**
     * Increment a metric value for a trace.
     *
     * @param traceName The name of the trace
     * @param metricName The name of the metric
     * @param incrementBy The amount to increment by (default: 1)
     */
    fun incrementMetric(
        traceName: String,
        metricName: String,
        incrementBy: Long = 1
    ) {
        logger.debug { "Incrementing metric '$metricName' by $incrementBy for trace '$traceName'" }
        performanceMonitoringService.incrementMetric(traceName, metricName, incrementBy)
    }

    /**
     * Record database query performance.
     *
     * @param traceName The name of the trace
     * @param queryTimeMs The query execution time in milliseconds
     * @param recordsReturned The number of records returned
     */
    fun recordDatabaseQuery(
        traceName: String,
        queryTimeMs: Long,
        recordsReturned: Int
    ) {
        putMetric(traceName, "db_query_time_ms", queryTimeMs)
        putMetric(traceName, "db_records_returned", recordsReturned.toLong())
        logger.debug { "Recorded database query: ${queryTimeMs}ms, $recordsReturned records" }
    }

    /**
     * Record location processing performance.
     *
     * @param traceName The name of the trace
     * @param processingTimeMs The processing time in milliseconds
     * @param pointsProcessed The number of location points processed
     */
    fun recordLocationProcessing(
        traceName: String,
        processingTimeMs: Long,
        pointsProcessed: Int
    ) {
        putMetric(traceName, "location_processing_time_ms", processingTimeMs)
        putMetric(traceName, "location_points_processed", pointsProcessed.toLong())
        logger.debug { "Recorded location processing: ${processingTimeMs}ms, $pointsProcessed points" }
    }

    /**
     * Record sync operation performance.
     *
     * @param traceName The name of the trace
     * @param syncDurationMs The sync duration in milliseconds
     * @param itemsSynced The number of items synced
     */
    fun recordSyncOperation(
        traceName: String,
        syncDurationMs: Long,
        itemsSynced: Int
    ) {
        putMetric(traceName, "sync_duration_ms", syncDurationMs)
        putMetric(traceName, "items_synced", itemsSynced.toLong())
        logger.debug { "Recorded sync operation: ${syncDurationMs}ms, $itemsSynced items" }
    }

    /**
     * Record network request performance.
     *
     * @param traceName The name of the trace
     * @param requestTimeMs The request time in milliseconds
     * @param responseSize The response size in bytes
     */
    fun recordNetworkRequest(
        traceName: String,
        requestTimeMs: Long,
        responseSize: Long
    ) {
        putMetric(traceName, "network_request_time_ms", requestTimeMs)
        putMetric(traceName, "network_response_size_bytes", responseSize)
        logger.debug { "Recorded network request: ${requestTimeMs}ms, $responseSize bytes" }
    }
}
