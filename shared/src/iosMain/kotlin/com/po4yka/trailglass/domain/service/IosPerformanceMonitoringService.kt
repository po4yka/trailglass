package com.po4yka.trailglass.domain.service

import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

private val logger = logger("IosPerformanceMonitoringService")

/**
 * iOS implementation of PerformanceMonitoringService using Firebase Performance.
 *
 * This service provides performance monitoring and trace tracking for the iOS platform.
 *
 * Note: Firebase iOS SDK integration requires CocoaPods configuration.
 * The actual Firebase setup must be done in the iosApp's AppDelegate.
 * See the iOS app code for Firebase initialization.
 */
@Inject
class IosPerformanceMonitoringService : PerformanceMonitoringService {
    override fun startTrace(traceName: String) {
        logger.warn { "iOS Performance Monitoring startTrace not yet implemented - requires Firebase iOS SDK integration" }
        logger.debug { "Trace name: $traceName" }
        // TODO: Implement via Firebase iOS SDK
        // let trace = Performance.startTrace(name: traceName)
        // Store trace reference in a map for later use
    }

    override fun stopTrace(traceName: String) {
        logger.warn { "iOS Performance Monitoring stopTrace not yet implemented - requires Firebase iOS SDK integration" }
        logger.debug { "Trace name: $traceName" }
        // TODO: Implement via Firebase iOS SDK
        // trace.stop()
    }

    override fun putMetric(
        traceName: String,
        metricName: String,
        value: Long
    ) {
        logger.debug { "Put metric '$metricName' = $value for trace '$traceName'" }
        // TODO: Implement via Firebase iOS SDK
        // trace.setMetric(metricName, value: value)
    }

    override fun incrementMetric(
        traceName: String,
        metricName: String,
        incrementBy: Long
    ) {
        logger.debug { "Increment metric '$metricName' by $incrementBy for trace '$traceName'" }
        // TODO: Implement via Firebase iOS SDK
        // trace.incrementMetric(metricName, by: incrementBy)
    }

    override fun putAttribute(
        traceName: String,
        attribute: String,
        value: String
    ) {
        logger.debug { "Put attribute '$attribute' = '$value' for trace '$traceName'" }
        // TODO: Implement via Firebase iOS SDK
        // trace.setValue(value, forAttribute: attribute)
    }

    override fun removeAttribute(
        traceName: String,
        attribute: String
    ) {
        logger.debug { "Remove attribute '$attribute' from trace '$traceName'" }
        // TODO: Implement via Firebase iOS SDK
        // trace.removeAttribute(attribute)
    }

    override fun getAttribute(
        traceName: String,
        attribute: String
    ): String? {
        logger.warn { "iOS getAttribute not yet implemented" }
        // TODO: Implement via Firebase iOS SDK
        // return trace.valueForAttribute(attribute)
        return null
    }

    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        logger.info { "Performance monitoring collection enabled: $enabled" }
        // TODO: Implement via Firebase iOS SDK
        // Performance.sharedInstance().isDataCollectionEnabled = enabled
    }

    override fun isPerformanceCollectionEnabled(): Boolean {
        logger.warn { "iOS isPerformanceCollectionEnabled not yet implemented" }
        // TODO: Implement via Firebase iOS SDK
        // return Performance.sharedInstance().isDataCollectionEnabled
        return false
    }
}
