package com.po4yka.trailglass.logging

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import platform.darwin.OS_LOG_DEFAULT
import platform.darwin.OS_LOG_TYPE_DEBUG
import platform.darwin.OS_LOG_TYPE_DEFAULT
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_FAULT
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal
import platform.darwin.os_log_create
import platform.darwin.os_log_t
import platform.darwin.os_log_type_enabled
import platform.darwin.os_log_type_t

/**
 * iOS logging configuration using Apple's os_log system.
 *
 * os_log provides:
 * - Structured logging with subsystems and categories
 * - Privacy-aware logging (sensitive data masking)
 * - Integration with Console.app and Instruments
 * - Better performance than NSLog
 *
 * Usage:
 * ```kotlin
 * // Get logger for a specific category
 * val osLog = OSLogConfiguration.getLogger("LocationTracking")
 *
 * // Log at different levels
 * OSLogConfiguration.log(osLog, OSLogLevel.INFO, "Location updated")
 * OSLogConfiguration.log(osLog, OSLogLevel.DEBUG, "Raw coordinates: $lat, $lon")
 * OSLogConfiguration.log(osLog, OSLogLevel.ERROR, "Failed to get location")
 * ```
 *
 * Viewing logs:
 * - Xcode Console: Automatically visible during debugging
 * - Console.app: Filter by subsystem "com.po4yka.trailglass"
 * - Terminal: `log stream --predicate 'subsystem == "com.po4yka.trailglass"'`
 */
object OSLogConfiguration {
    /** The app's bundle identifier used as os_log subsystem */
    private const val SUBSYSTEM = "com.po4yka.trailglass"

    /** Cache of created loggers by category */
    private val loggers = mutableMapOf<String, os_log_t>()

    /** Default logger for general app logging */
    val defaultLogger: os_log_t by lazy { getLogger("Default") }

    /**
     * Gets or creates an os_log instance for the specified category.
     *
     * @param category The logging category (e.g., "LocationTracking", "Sync", "Database")
     * @return An os_log_t instance for the category
     */
    @OptIn(ExperimentalForeignApi::class)
    fun getLogger(category: String): os_log_t {
        return loggers.getOrPut(category) {
            os_log_create(SUBSYSTEM, category) ?: OS_LOG_DEFAULT
        }
    }

    /**
     * Logs a message using os_log.
     *
     * @param logger The os_log instance to use
     * @param level The log level
     * @param message The message to log
     */
    @OptIn(ExperimentalForeignApi::class)
    fun log(logger: os_log_t, level: OSLogLevel, message: String) {
        val osLogType = level.toOSLogType()

        // Check if this log level is enabled before formatting
        if (!os_log_type_enabled(logger, osLogType)) {
            return
        }

        // Use _os_log_internal for direct logging
        // Note: This is a simplified approach. For production, consider using
        // a Swift helper for better os_log format string support.
        _os_log_internal(
            __dso_handle.ptr,
            logger,
            osLogType,
            message
        )
    }

    /**
     * Logs a message at DEBUG level.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun debug(logger: os_log_t, message: String) {
        log(logger, OSLogLevel.DEBUG, message)
    }

    /**
     * Logs a message at INFO level.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun info(logger: os_log_t, message: String) {
        log(logger, OSLogLevel.INFO, message)
    }

    /**
     * Logs a message at DEFAULT level (similar to notice).
     */
    @OptIn(ExperimentalForeignApi::class)
    fun notice(logger: os_log_t, message: String) {
        log(logger, OSLogLevel.DEFAULT, message)
    }

    /**
     * Logs a message at ERROR level.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun error(logger: os_log_t, message: String) {
        log(logger, OSLogLevel.ERROR, message)
    }

    /**
     * Logs a message at FAULT level (critical errors).
     */
    @OptIn(ExperimentalForeignApi::class)
    fun fault(logger: os_log_t, message: String) {
        log(logger, OSLogLevel.FAULT, message)
    }

    /**
     * Checks if a specific log level is enabled for the logger.
     * Useful for expensive log message construction.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun isEnabled(logger: os_log_t, level: OSLogLevel): Boolean {
        return os_log_type_enabled(logger, level.toOSLogType())
    }
}

/**
 * Log levels for os_log, mapping to Apple's os_log_type_t.
 */
enum class OSLogLevel {
    /** For detailed debugging during development */
    DEBUG,

    /** For informational messages */
    INFO,

    /** Default level for general messages */
    DEFAULT,

    /** For error conditions */
    ERROR,

    /** For critical errors that may cause data loss or crashes */
    FAULT;

    /**
     * Converts to Apple's os_log_type_t.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun toOSLogType(): os_log_type_t = when (this) {
        DEBUG -> OS_LOG_TYPE_DEBUG
        INFO -> OS_LOG_TYPE_INFO
        DEFAULT -> OS_LOG_TYPE_DEFAULT
        ERROR -> OS_LOG_TYPE_ERROR
        FAULT -> OS_LOG_TYPE_FAULT
    }
}

/**
 * Extension function to bridge kotlin-logging to os_log.
 *
 * Usage:
 * ```kotlin
 * val logger = logger("MyClass")
 * logger.infoWithOSLog("LocationTracking") { "Location updated" }
 * ```
 */
@OptIn(ExperimentalForeignApi::class)
fun KLogger.debugWithOSLog(category: String, message: () -> String) {
    debug(message)
    OSLogConfiguration.debug(OSLogConfiguration.getLogger(category), message())
}

@OptIn(ExperimentalForeignApi::class)
fun KLogger.infoWithOSLog(category: String, message: () -> String) {
    info(message)
    OSLogConfiguration.info(OSLogConfiguration.getLogger(category), message())
}

@OptIn(ExperimentalForeignApi::class)
fun KLogger.warnWithOSLog(category: String, message: () -> String) {
    warn(message)
    OSLogConfiguration.error(OSLogConfiguration.getLogger(category), message())
}

@OptIn(ExperimentalForeignApi::class)
fun KLogger.errorWithOSLog(category: String, message: () -> String) {
    error(message)
    OSLogConfiguration.error(OSLogConfiguration.getLogger(category), message())
}
