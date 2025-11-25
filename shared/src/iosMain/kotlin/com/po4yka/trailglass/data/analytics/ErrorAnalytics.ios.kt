package com.po4yka.trailglass.data.analytics

import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.logging.logger

private val logger = logger("ErrorAnalytics")

/**
 * iOS implementation of ErrorAnalytics.
 *
 * Can be extended to integrate with Firebase Crashlytics or other analytics platforms.
 */
actual object ErrorAnalyticsFactory {
    actual fun create(): ErrorAnalytics {
        // Default to logging implementation
        // Can be replaced with Firebase Crashlytics:
        // return IOSErrorAnalytics()
        return LoggingErrorAnalytics()
    }
}

/**
 * iOS-specific analytics implementation using kotlin-logging.
 *
 * This implementation can integrate with Firebase Crashlytics while using
 * kotlin-logging for consistent logging across the project.
 *
 * To enable Crashlytics:
 * 1. Add Firebase Crashlytics iOS SDK via CocoaPods
 * 2. Create Swift helper for Crashlytics interop
 * 3. Replace LoggingErrorAnalytics with IOSErrorAnalytics in ErrorAnalyticsFactory
 */
class IOSErrorAnalytics : ErrorAnalytics {
    override fun logError(error: TrailGlassError, context: Map<String, Any>) {
        logger.error { "ERROR: ${error.getTechnicalDetails()}" }
        // TODO: Integrate with Crashlytics via Swift interop
        // FirebaseCrashlyticsHelper.recordError(error)
    }

    override fun logNonFatal(error: TrailGlassError, context: Map<String, Any>) {
        logger.warn { "NON_FATAL: ${error.userMessage}" }
        // TODO: Integrate with Crashlytics via Swift interop
        // FirebaseCrashlyticsHelper.recordNonFatal(error)
    }

    override fun logFatal(error: TrailGlassError, context: Map<String, Any>) {
        logger.error { "FATAL: ${error.getTechnicalDetails()}" }
        // TODO: Integrate with Crashlytics via Swift interop
        // FirebaseCrashlyticsHelper.recordFatal(error)
    }

    override fun setUserId(userId: String?) {
        logger.debug { "Setting user ID: ${userId ?: "null"}" }
        // TODO: Integrate with Crashlytics via Swift interop
        // FirebaseCrashlyticsHelper.setUserId(userId)
    }

    override fun setCustomData(key: String, value: String) {
        logger.debug { "Setting custom data: $key = $value" }
        // TODO: Integrate with Crashlytics via Swift interop
        // FirebaseCrashlyticsHelper.setCustomKey(key, value)
    }

    override fun clearCustomData() {
        logger.debug { "Clearing custom data" }
        // TODO: Integrate with Crashlytics via Swift interop
        // FirebaseCrashlyticsHelper.clearCustomData()
    }
}
