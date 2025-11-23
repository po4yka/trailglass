package com.po4yka.trailglass.domain.service

/**
 * Service for performance monitoring and trace tracking.
 *
 * This is a platform-specific service that handles performance monitoring using Firebase Performance.
 * Platform implementations:
 * - Android: Firebase Performance Monitoring
 * - iOS: Firebase Performance Monitoring (iOS SDK)
 *
 * Use this service to track custom traces and metrics for application performance analysis.
 */
interface PerformanceMonitoringService {
    /**
     * Start a custom trace.
     *
     * Traces are used to measure the time between two points in your code.
     * You must call stopTrace() with the same trace name to complete the trace.
     *
     * @param traceName The name of the trace to start
     */
    fun startTrace(traceName: String)

    /**
     * Stop a custom trace.
     *
     * Completes a trace that was started with startTrace().
     *
     * @param traceName The name of the trace to stop
     */
    fun stopTrace(traceName: String)

    /**
     * Record a metric value for a trace.
     *
     * Metrics are used to capture performance data associated with a trace.
     * The trace must be started before recording a metric.
     *
     * @param traceName The name of the trace
     * @param metricName The name of the metric
     * @param value The metric value
     */
    fun putMetric(
        traceName: String,
        metricName: String,
        value: Long
    )

    /**
     * Increment a metric value for a trace.
     *
     * @param traceName The name of the trace
     * @param metricName The name of the metric to increment
     * @param incrementBy The amount to increment by (default: 1)
     */
    fun incrementMetric(
        traceName: String,
        metricName: String,
        incrementBy: Long = 1
    )

    /**
     * Set a custom attribute for a trace.
     *
     * Attributes are used to segment performance data.
     *
     * @param traceName The name of the trace
     * @param attribute The attribute name
     * @param value The attribute value
     */
    fun putAttribute(
        traceName: String,
        attribute: String,
        value: String
    )

    /**
     * Remove a custom attribute from a trace.
     *
     * @param traceName The name of the trace
     * @param attribute The attribute name to remove
     */
    fun removeAttribute(
        traceName: String,
        attribute: String
    )

    /**
     * Get the value of a custom attribute.
     *
     * @param traceName The name of the trace
     * @param attribute The attribute name
     * @return The attribute value, or null if not found
     */
    fun getAttribute(
        traceName: String,
        attribute: String
    ): String?

    /**
     * Enable or disable performance monitoring.
     *
     * @param enabled true to enable, false to disable
     */
    fun setPerformanceCollectionEnabled(enabled: Boolean)

    /**
     * Check if performance monitoring is enabled.
     *
     * @return true if enabled, false otherwise
     */
    fun isPerformanceCollectionEnabled(): Boolean
}
