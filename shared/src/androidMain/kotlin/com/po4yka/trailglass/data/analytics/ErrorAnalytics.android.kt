package com.po4yka.trailglass.data.analytics

/**
 * Android implementation of ErrorAnalytics.
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

// Example Firebase Crashlytics implementation (commented out):
/*
class FirebaseCrashlyticsErrorAnalytics : ErrorAnalytics {
    private val crashlytics = Firebase.crashlytics

    override fun logError(error: TrailGlassError, context: Map<String, Any>) {
        crashlytics.recordException(
            Exception(error.userMessage, error.cause)
        )
        context.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value.toString())
        }
    }

    override fun logNonFatal(error: TrailGlassError, context: Map<String, Any>) {
        logError(error, context)
    }

    override fun logFatal(error: TrailGlassError, context: Map<String, Any>) {
        crashlytics.log("FATAL: ${error.getTechnicalDetails()}")
        logError(error, context)
    }

    override fun setUserId(userId: String?) {
        crashlytics.setUserId(userId ?: "")
    }

    override fun setCustomData(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun clearCustomData() {
        // Crashlytics doesn't support clearing individual keys
    }
}
*/
