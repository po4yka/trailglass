package com.po4yka.trailglass.feature.performance

import com.po4yka.trailglass.domain.service.PerformanceMonitoringService
import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * Use case for tracking performance traces.
 *
 * This use case provides a convenient way to start and stop custom performance traces
 * for measuring the time between two points in code.
 *
 * Example usage:
 * ```
 * trackPerformanceTraceUseCase.start("location_processing")
 * // ... perform location processing ...
 * trackPerformanceTraceUseCase.stop("location_processing")
 * ```
 */
@Inject
class TrackPerformanceTraceUseCase(
    private val performanceMonitoringService: PerformanceMonitoringService
) {
    /**
     * Start a custom performance trace.
     *
     * @param traceName The name of the trace to start
     */
    fun start(traceName: String) {
        logger.debug { "Starting performance trace: $traceName" }
        performanceMonitoringService.startTrace(traceName)
    }

    /**
     * Stop a custom performance trace.
     *
     * @param traceName The name of the trace to stop
     */
    fun stop(traceName: String) {
        logger.debug { "Stopping performance trace: $traceName" }
        performanceMonitoringService.stopTrace(traceName)
    }

    /**
     * Execute a block of code within a performance trace.
     *
     * Automatically starts the trace before execution and stops it after.
     *
     * @param traceName The name of the trace
     * @param block The code block to execute
     * @return The result of the block execution
     */
    inline fun <T> trace(
        traceName: String,
        block: () -> T
    ): T {
        start(traceName)
        return try {
            block()
        } finally {
            stop(traceName)
        }
    }

    /**
     * Execute a suspending block of code within a performance trace.
     *
     * Automatically starts the trace before execution and stops it after.
     *
     * @param traceName The name of the trace
     * @param block The suspending code block to execute
     * @return The result of the block execution
     */
    suspend inline fun <T> traceSuspend(
        traceName: String,
        crossinline block: suspend () -> T
    ): T {
        start(traceName)
        return try {
            block()
        } finally {
            stop(traceName)
        }
    }

    /**
     * Set a custom attribute for a trace.
     *
     * @param traceName The name of the trace
     * @param attribute The attribute name
     * @param value The attribute value
     */
    fun putAttribute(
        traceName: String,
        attribute: String,
        value: String
    ) {
        performanceMonitoringService.putAttribute(traceName, attribute, value)
    }

    /**
     * Remove a custom attribute from a trace.
     *
     * @param traceName The name of the trace
     * @param attribute The attribute name
     */
    fun removeAttribute(
        traceName: String,
        attribute: String
    ) {
        performanceMonitoringService.removeAttribute(traceName, attribute)
    }
}
