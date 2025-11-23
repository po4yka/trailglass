package com.po4yka.trailglass.domain.service

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * Android implementation of PerformanceMonitoringService using Firebase Performance.
 *
 * This service provides performance monitoring and trace tracking for the Android platform.
 */
@Inject
class AndroidPerformanceMonitoringService : PerformanceMonitoringService {
    private val performance: FirebasePerformance = FirebasePerformance.getInstance()
    private val activeTraces = mutableMapOf<String, Trace>()

    override fun startTrace(traceName: String) {
        try {
            if (activeTraces.containsKey(traceName)) {
                logger.warn { "Trace '$traceName' is already active" }
                return
            }

            val trace = performance.newTrace(traceName)
            trace.start()
            activeTraces[traceName] = trace
            logger.debug { "Started performance trace: $traceName" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to start trace '$traceName': ${e.message}" }
        }
    }

    override fun stopTrace(traceName: String) {
        try {
            val trace = activeTraces.remove(traceName)
            if (trace == null) {
                logger.warn { "Trace '$traceName' was not found or already stopped" }
                return
            }

            trace.stop()
            logger.debug { "Stopped performance trace: $traceName" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to stop trace '$traceName': ${e.message}" }
        }
    }

    override fun putMetric(
        traceName: String,
        metricName: String,
        value: Long
    ) {
        try {
            val trace = activeTraces[traceName]
            if (trace == null) {
                logger.warn { "Cannot put metric '$metricName' - trace '$traceName' is not active" }
                return
            }

            trace.putMetric(metricName, value)
            logger.debug { "Put metric '$metricName' = $value for trace '$traceName'" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to put metric '$metricName' for trace '$traceName': ${e.message}" }
        }
    }

    override fun incrementMetric(
        traceName: String,
        metricName: String,
        incrementBy: Long
    ) {
        try {
            val trace = activeTraces[traceName]
            if (trace == null) {
                logger.warn { "Cannot increment metric '$metricName' - trace '$traceName' is not active" }
                return
            }

            trace.incrementMetric(metricName, incrementBy)
            logger.debug { "Incremented metric '$metricName' by $incrementBy for trace '$traceName'" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to increment metric '$metricName' for trace '$traceName': ${e.message}" }
        }
    }

    override fun putAttribute(
        traceName: String,
        attribute: String,
        value: String
    ) {
        try {
            val trace = activeTraces[traceName]
            if (trace == null) {
                logger.warn { "Cannot put attribute '$attribute' - trace '$traceName' is not active" }
                return
            }

            trace.putAttribute(attribute, value)
            logger.debug { "Put attribute '$attribute' = '$value' for trace '$traceName'" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to put attribute '$attribute' for trace '$traceName': ${e.message}" }
        }
    }

    override fun removeAttribute(
        traceName: String,
        attribute: String
    ) {
        try {
            val trace = activeTraces[traceName]
            if (trace == null) {
                logger.warn { "Cannot remove attribute '$attribute' - trace '$traceName' is not active" }
                return
            }

            trace.removeAttribute(attribute)
            logger.debug { "Removed attribute '$attribute' from trace '$traceName'" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to remove attribute '$attribute' for trace '$traceName': ${e.message}" }
        }
    }

    override fun getAttribute(
        traceName: String,
        attribute: String
    ): String? {
        return try {
            val trace = activeTraces[traceName]
            if (trace == null) {
                logger.warn { "Cannot get attribute '$attribute' - trace '$traceName' is not active" }
                return null
            }

            trace.getAttribute(attribute)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get attribute '$attribute' for trace '$traceName': ${e.message}" }
            null
        }
    }

    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        try {
            performance.isPerformanceCollectionEnabled = enabled
            logger.info { "Performance monitoring collection enabled: $enabled" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to set performance collection enabled: ${e.message}" }
        }
    }

    override fun isPerformanceCollectionEnabled(): Boolean =
        try {
            performance.isPerformanceCollectionEnabled
        } catch (e: Exception) {
            logger.error(e) { "Failed to check if performance collection is enabled: ${e.message}" }
            false
        }
}
