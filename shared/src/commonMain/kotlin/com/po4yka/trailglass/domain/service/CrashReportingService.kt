package com.po4yka.trailglass.domain.service

/**
 * Service for crash reporting and error tracking.
 *
 * This is a platform-specific service that handles crash reporting using Firebase Crashlytics.
 * Platform implementations:
 * - Android: Firebase Crashlytics
 * - iOS: Firebase Crashlytics
 */
interface CrashReportingService {
    /**
     * Log a non-fatal exception.
     *
     * Use this to record exceptions that are caught and handled,
     * but still indicate something unexpected happened.
     *
     * @param throwable The exception to log
     */
    fun recordException(throwable: Throwable)

    /**
     * Log a message to Crashlytics.
     *
     * @param message The message to log
     */
    fun log(message: String)

    /**
     * Set a custom key-value pair.
     *
     * Custom keys help you get the specific state of your app leading up to a crash.
     *
     * @param key The key for the custom value
     * @param value The value (String, Int, Boolean, Float, Double, Long)
     */
    fun setCustomKey(
        key: String,
        value: String
    )

    fun setCustomKey(
        key: String,
        value: Int
    )

    fun setCustomKey(
        key: String,
        value: Boolean
    )

    fun setCustomKey(
        key: String,
        value: Float
    )

    fun setCustomKey(
        key: String,
        value: Double
    )

    fun setCustomKey(
        key: String,
        value: Long
    )

    /**
     * Set the user identifier.
     *
     * Sets a user ID for tracking and filtering crashes by user.
     *
     * @param userId The user identifier
     */
    fun setUserId(userId: String)

    /**
     * Enable or disable crash collection.
     *
     * @param enabled true to enable, false to disable
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)

    /**
     * Check if crash collection is enabled.
     *
     * @return true if enabled, false otherwise
     */
    fun isCrashlyticsCollectionEnabled(): Boolean

    /**
     * Force send any unsent crash reports.
     *
     * Useful for testing or ensuring reports are sent before app termination.
     */
    fun sendUnsentReports()

    /**
     * Delete any unsent crash reports.
     *
     * Useful for privacy compliance when user opts out.
     */
    fun deleteUnsentReports()

    /**
     * Check if there are any unsent crash reports.
     *
     * @return true if there are unsent reports, false otherwise
     */
    fun didCrashOnPreviousExecution(): Boolean
}
