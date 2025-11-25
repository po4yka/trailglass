package com.po4yka.trailglass.logging

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.Level

/**
 * Centralized logging configuration for the TrailGlass application.
 *
 * This provides environment-aware logging level management across platforms.
 *
 * Usage:
 * ```kotlin
 * // At app startup
 * LoggingConfig.initialize(isDebugBuild = BuildConfig.DEBUG)
 *
 * // Check if a level is enabled before expensive operations
 * if (LoggingConfig.isDebugEnabled) {
 *     logger.debug { expensiveComputation() }
 * }
 *
 * // Get minimum log level
 * val minLevel = LoggingConfig.minimumLogLevel
 * ```
 */
object LoggingConfig {
    /**
     * Whether the app is running in debug mode.
     * Set during initialization.
     */
    var isDebugBuild: Boolean = false
        private set

    /**
     * The minimum log level that will be output.
     * DEBUG and TRACE are only shown in debug builds.
     */
    val minimumLogLevel: Level
        get() = if (isDebugBuild) Level.DEBUG else Level.INFO

    /**
     * Whether DEBUG level logging is enabled.
     */
    val isDebugEnabled: Boolean
        get() = isDebugBuild

    /**
     * Whether TRACE level logging is enabled.
     * TRACE is only enabled in debug builds with explicit configuration.
     */
    val isTraceEnabled: Boolean
        get() = isDebugBuild && traceEnabledOverride

    /**
     * Override to enable TRACE logging even in release builds (for diagnostics).
     * Use with caution as it may expose sensitive data.
     */
    var traceEnabledOverride: Boolean = false

    /**
     * Per-module logging overrides.
     * Allows enabling debug logging for specific modules in release builds.
     */
    private val moduleOverrides = mutableMapOf<String, Level>()

    /**
     * Initializes the logging configuration.
     *
     * Call this early in app startup, typically in Application.onCreate (Android)
     * or AppDelegate.didFinishLaunching (iOS).
     *
     * @param isDebugBuild Whether this is a debug build (BuildConfig.DEBUG on Android)
     */
    fun initialize(isDebugBuild: Boolean) {
        this.isDebugBuild = isDebugBuild

        val buildType = if (isDebugBuild) "DEBUG" else "RELEASE"
        // Using println here since logger may not be fully initialized yet
        println("[LoggingConfig] Initialized: buildType=$buildType, minLevel=${minimumLogLevel.name}")
    }

    /**
     * Sets a logging level override for a specific module/tag.
     *
     * @param moduleName The module name (e.g., "LocationTracking", "Sync")
     * @param level The minimum level to log for this module
     */
    fun setModuleLevel(moduleName: String, level: Level) {
        moduleOverrides[moduleName] = level
    }

    /**
     * Gets the effective logging level for a module.
     *
     * @param moduleName The module name
     * @return The effective minimum log level for this module
     */
    fun getModuleLevel(moduleName: String): Level {
        return moduleOverrides[moduleName] ?: minimumLogLevel
    }

    /**
     * Clears all module-specific overrides.
     */
    fun clearModuleOverrides() {
        moduleOverrides.clear()
    }

    /**
     * Checks if logging at the specified level is enabled for a module.
     *
     * @param moduleName The module name
     * @param level The level to check
     * @return true if logging at this level is enabled
     */
    fun isLevelEnabled(moduleName: String, level: Level): Boolean {
        val effectiveLevel = getModuleLevel(moduleName)
        return level.ordinal >= effectiveLevel.ordinal
    }
}

/**
 * Extension function to check if a KLogger should log at DEBUG level.
 * Use this for expensive log message construction.
 *
 * Example:
 * ```kotlin
 * if (logger.isDebugEnabledFor("LocationTracking")) {
 *     logger.debug { expensiveLocationFormat() }
 * }
 * ```
 */
fun KLogger.isDebugEnabledFor(moduleName: String): Boolean {
    return LoggingConfig.isLevelEnabled(moduleName, Level.DEBUG)
}

/**
 * Extension function to check if a KLogger should log at INFO level.
 */
fun KLogger.isInfoEnabledFor(moduleName: String): Boolean {
    return LoggingConfig.isLevelEnabled(moduleName, Level.INFO)
}

/**
 * Conditional debug logging that respects LoggingConfig.
 * Only evaluates the message if debug is enabled for the module.
 */
inline fun KLogger.debugIfEnabled(moduleName: String, crossinline message: () -> String) {
    if (LoggingConfig.isLevelEnabled(moduleName, Level.DEBUG)) {
        debug { message() }
    }
}

/**
 * Conditional trace logging that respects LoggingConfig.
 * Only evaluates the message if trace is enabled.
 */
inline fun KLogger.traceIfEnabled(moduleName: String, crossinline message: () -> String) {
    if (LoggingConfig.isLevelEnabled(moduleName, Level.TRACE)) {
        trace { message() }
    }
}
