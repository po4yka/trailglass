package com.po4yka.trailglass.data.analytics

/**
 * iOS implementation of ErrorAnalytics.
 *
 * Can be extended to integrate with Firebase Crashlytics or other analytics platforms.
 */
actual object ErrorAnalyticsFactory {
    actual fun create(): ErrorAnalytics {
        // Default to logging implementation
        // Can be replaced with Firebase Crashlytics:
        // return FirebaseCrashlyticsErrorAnalytics()
        return LoggingErrorAnalytics()
    }
}

// Example iOS-specific analytics implementation (commented out):
/*
class IOSErrorAnalytics : ErrorAnalytics {
    // Integrate with NSLog or OS Logger
    override fun logError(error: TrailGlassError, context: Map<String, Any>) {
        NSLog("ERROR: ${error.getTechnicalDetails()}")
        // Could integrate with Crashlytics or other iOS analytics
    }

    override fun logNonFatal(error: TrailGlassError, context: Map<String, Any>) {
        NSLog("NON_FATAL: ${error.userMessage}")
    }

    override fun logFatal(error: TrailGlassError, context: Map<String, Any>) {
        NSLog("FATAL: ${error.getTechnicalDetails()}")
    }

    override fun setUserId(userId: String?) {
        // Set user identifier
    }

    override fun setCustomData(key: String, value: String) {
        // Set custom data
    }

    override fun clearCustomData() {
        // Clear custom data
    }
}
*/
