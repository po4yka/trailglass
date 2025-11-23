package com.po4yka.trailglass.crash

import com.po4yka.trailglass.domain.service.CrashReportingService
import com.po4yka.trailglass.logging.logger

/**
 * Custom exception handler for logging uncaught exceptions to Crashlytics.
 *
 * This handler wraps the platform's default exception handler and ensures
 * all uncaught exceptions are logged to Crashlytics before the app terminates.
 */
class CrashHandler(
    private val crashReportingService: CrashReportingService
) {
    private val logger = logger()
    private var originalHandler: ((Throwable) -> Unit)? = null
    private var isInstalled = false

    /**
     * Install the crash handler.
     *
     * This should be called early in the app initialization, typically in the
     * Application class (Android) or main app file (iOS).
     */
    fun install() {
        if (isInstalled) {
            logger.warn { "CrashHandler already installed" }
            return
        }

        logger.info { "Installing CrashHandler" }

        // Save the original exception handler
        originalHandler = getDefaultExceptionHandler()

        // Set our custom handler
        setDefaultExceptionHandler { throwable ->
            handleUncaughtException(throwable)
        }

        isInstalled = true
    }

    /**
     * Uninstall the crash handler.
     *
     * This restores the original exception handler.
     */
    fun uninstall() {
        if (!isInstalled) {
            logger.warn { "CrashHandler not installed" }
            return
        }

        logger.info { "Uninstalling CrashHandler" }

        // Restore the original handler
        originalHandler?.let { setDefaultExceptionHandler(it) }

        isInstalled = false
    }

    private fun handleUncaughtException(throwable: Throwable) {
        try {
            logger.error(throwable) { "Uncaught exception: ${throwable.message}" }

            // Log to Crashlytics
            crashReportingService.log("Uncaught exception: ${throwable.message}")
            crashReportingService.recordException(throwable)

            // Force send the report
            crashReportingService.sendUnsentReports()
        } catch (e: Exception) {
            logger.error(e) { "Error logging crash: ${e.message}" }
        } finally {
            // Call the original handler to perform platform-specific cleanup
            originalHandler?.invoke(throwable)
        }
    }
}

/**
 * Get the default platform exception handler.
 *
 * Platform-specific implementation required.
 */
expect fun getDefaultExceptionHandler(): ((Throwable) -> Unit)?

/**
 * Set the default platform exception handler.
 *
 * Platform-specific implementation required.
 */
expect fun setDefaultExceptionHandler(handler: (Throwable) -> Unit)
