package com.po4yka.trailglass.data.analytics

import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Error analytics and reporting system.
 *
 * Tracks errors for debugging, analytics, and user feedback.
 */
interface ErrorAnalytics {
    /**
     * Log an error for analytics.
     */
    fun logError(error: TrailGlassError, context: Map<String, Any> = emptyMap())

    /**
     * Log a non-fatal error.
     */
    fun logNonFatal(error: TrailGlassError, context: Map<String, Any> = emptyMap())

    /**
     * Log a fatal error (app crash).
     */
    fun logFatal(error: TrailGlassError, context: Map<String, Any> = emptyMap())

    /**
     * Set user identifier for error tracking.
     */
    fun setUserId(userId: String?)

    /**
     * Add custom context data to all error reports.
     */
    fun setCustomData(key: String, value: String)

    /**
     * Clear custom context data.
     */
    fun clearCustomData()
}

/**
 * Default implementation of ErrorAnalytics using logging.
 */
class LoggingErrorAnalytics : ErrorAnalytics {
    private val logger = logger()
    private val customData = mutableMapOf<String, String>()
    private var userId: String? = null

    override fun logError(error: TrailGlassError, context: Map<String, Any>) {
        logger.error {
            buildErrorMessage(error, context, "ERROR")
        }
    }

    override fun logNonFatal(error: TrailGlassError, context: Map<String, Any>) {
        logger.warn {
            buildErrorMessage(error, context, "NON_FATAL")
        }
    }

    override fun logFatal(error: TrailGlassError, context: Map<String, Any>) {
        logger.error {
            buildErrorMessage(error, context, "FATAL")
        }
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }

    override fun setCustomData(key: String, value: String) {
        customData[key] = value
    }

    override fun clearCustomData() {
        customData.clear()
    }

    private fun buildErrorMessage(
        error: TrailGlassError,
        context: Map<String, Any>,
        severity: String
    ): String {
        return buildString {
            appendLine("[$severity] ${error.errorCode ?: "UNK"}")
            appendLine("User Message: ${error.userMessage}")
            appendLine("Technical: ${error.getTechnicalDetails()}")

            if (userId != null) {
                appendLine("User ID: $userId")
            }

            if (customData.isNotEmpty()) {
                appendLine("Custom Data: $customData")
            }

            if (context.isNotEmpty()) {
                appendLine("Context: $context")
            }

            error.cause?.let { cause ->
                appendLine("Cause: ${cause.stackTraceToString()}")
            }
        }
    }
}

/**
 * Error analytics factory.
 */
expect object ErrorAnalyticsFactory {
    fun create(): ErrorAnalytics
}

/**
 * Error event for tracking.
 */
data class ErrorEvent(
    val error: TrailGlassError,
    val timestamp: Instant = Clock.System.now(),
    val context: Map<String, Any> = emptyMap(),
    val userId: String? = null,
    val severity: ErrorSeverity = ErrorSeverity.ERROR
)

/**
 * Error severity levels.
 */
enum class ErrorSeverity {
    /** Informational errors that don't affect functionality */
    INFO,

    /** Warnings that might indicate problems */
    WARNING,

    /** Errors that affect functionality but don't crash the app */
    ERROR,

    /** Critical errors that might crash the app */
    FATAL
}

/**
 * Extension function to log errors with analytics.
 */
fun TrailGlassError.logToAnalytics(
    analytics: ErrorAnalytics,
    context: Map<String, Any> = emptyMap(),
    severity: ErrorSeverity = ErrorSeverity.ERROR
) {
    when (severity) {
        ErrorSeverity.INFO,
        ErrorSeverity.WARNING -> analytics.logNonFatal(this, context)
        ErrorSeverity.ERROR -> analytics.logError(this, context)
        ErrorSeverity.FATAL -> analytics.logFatal(this, context)
    }
}
