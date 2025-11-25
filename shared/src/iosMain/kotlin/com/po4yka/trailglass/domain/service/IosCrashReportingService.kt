package com.po4yka.trailglass.domain.service

import com.po4yka.trailglass.logging.LogBuffer
import com.po4yka.trailglass.logging.LogLevel
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

private val logger = logger("IosCrashReportingService")

/**
 * iOS implementation of CrashReportingService.
 *
 * This service provides crash reporting and error tracking for the iOS platform.
 * It integrates with LogBuffer for breadcrumb tracking and error logging.
 *
 * **Implementation Note:**
 * For full Firebase Crashlytics integration, the Swift CrashReporter helper
 * in the iOS app bridges to this service. Exception recording and breadcrumb
 * logging flow through LogBuffer for in-app visibility and crash analysis.
 *
 * The Swift-side CrashReporter.swift handles:
 * - NSException catching
 * - Signal handling (SIGABRT, SIGSEGV, etc.)
 * - os_log integration
 *
 * This Kotlin service handles:
 * - LogBuffer integration for breadcrumbs
 * - Custom key storage for crash context
 * - User identification for crash grouping
 */
@Inject
class IosCrashReportingService : CrashReportingService {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var crashlyticsEnabled = true
    private var userId: String? = null
    private val customKeys = mutableMapOf<String, Any>()
    private var crashedOnPreviousExecution = false
    override fun recordException(throwable: Throwable) {
        logger.error(throwable) { "Recording exception: ${throwable.message}" }

        // Log to LogBuffer with stack trace for crash analysis
        scope.launch {
            LogBuffer.addError(
                tag = "CrashReporting",
                message = "Exception recorded: ${throwable.message}",
                throwable = throwable
            )
        }
    }

    override fun log(message: String) {
        logger.debug { "Crashlytics log: $message" }

        // Add as breadcrumb to LogBuffer
        scope.launch {
            LogBuffer.addBreadcrumb(
                tag = "CrashReporting",
                message = message
            )
        }
    }

    override fun setCustomKeyString(
        key: String,
        value: String
    ) {
        customKeys[key] = value
        logger.debug { "Set custom key: $key = $value" }
    }

    override fun setCustomKeyInt(
        key: String,
        value: Int
    ) {
        customKeys[key] = value
        logger.debug { "Set custom key: $key = $value" }
    }

    override fun setCustomKeyBool(
        key: String,
        value: Boolean
    ) {
        customKeys[key] = value
        logger.debug { "Set custom key: $key = $value" }
    }

    override fun setCustomKeyFloat(
        key: String,
        value: Float
    ) {
        customKeys[key] = value
        logger.debug { "Set custom key: $key = $value" }
    }

    override fun setCustomKeyDouble(
        key: String,
        value: Double
    ) {
        customKeys[key] = value
        logger.debug { "Set custom key: $key = $value" }
    }

    override fun setCustomKeyLong(
        key: String,
        value: Long
    ) {
        customKeys[key] = value
        logger.debug { "Set custom key: $key = $value" }
    }

    override fun setUserId(userId: String) {
        this.userId = userId
        logger.info { "Set crash reporting user ID: $userId" }

        scope.launch {
            LogBuffer.addBreadcrumb(
                tag = "CrashReporting",
                message = "User ID set: $userId"
            )
        }
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlyticsEnabled = enabled
        logger.info { "Crashlytics collection enabled: $enabled" }

        scope.launch {
            LogBuffer.addBreadcrumb(
                tag = "CrashReporting",
                message = "Crash collection ${if (enabled) "enabled" else "disabled"}"
            )
        }
    }

    override fun isCrashlyticsCollectionEnabled(): Boolean {
        return crashlyticsEnabled
    }

    override fun sendUnsentReports() {
        logger.info { "Sending unsent crash reports" }
        // Log breadcrumb for audit trail
        scope.launch {
            LogBuffer.addBreadcrumb(
                tag = "CrashReporting",
                message = "Unsent reports send requested"
            )
        }
    }

    override fun deleteUnsentReports() {
        logger.info { "Deleting unsent crash reports" }
        scope.launch {
            LogBuffer.addBreadcrumb(
                tag = "CrashReporting",
                message = "Unsent reports deleted"
            )
        }
    }

    override fun didCrashOnPreviousExecution(): Boolean {
        return crashedOnPreviousExecution
    }

    /**
     * Mark that a crash occurred during previous execution.
     * Called from Swift CrashReporter when crash data is detected.
     */
    fun markCrashedOnPreviousExecution() {
        crashedOnPreviousExecution = true
        logger.warn { "Previous execution crashed" }
    }

    /**
     * Get all custom keys for crash context.
     */
    fun getCustomKeys(): Map<String, Any> = customKeys.toMap()

    /**
     * Get the current user ID if set.
     */
    fun getCurrentUserId(): String? = userId
}
