package com.po4yka.trailglass.feature.crashreporting

import com.po4yka.trailglass.domain.service.CrashReportingService
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for managing crash reporting settings.
 *
 * This allows users to opt-in/opt-out of crash reporting for privacy compliance.
 */
@Inject
class ManageCrashReportingUseCase(
    private val crashReportingService: CrashReportingService
) {
    private val logger = logger()

    /**
     * Enable or disable crash reporting collection.
     *
     * @param enabled true to enable, false to disable
     */
    fun setCrashReportingEnabled(enabled: Boolean) {
        logger.info { "Setting crash reporting enabled: $enabled" }
        crashReportingService.setCrashlyticsCollectionEnabled(enabled)

        if (!enabled) {
            // Delete any unsent reports when user opts out
            crashReportingService.deleteUnsentReports()
        }
    }

    /**
     * Check if crash reporting is enabled.
     *
     * @return true if enabled, false otherwise
     */
    fun isCrashReportingEnabled(): Boolean = crashReportingService.isCrashlyticsCollectionEnabled()

    /**
     * Send any unsent crash reports.
     *
     * Useful for testing or ensuring reports are sent before app termination.
     */
    fun sendUnsentReports() {
        logger.info { "Sending unsent crash reports" }
        crashReportingService.sendUnsentReports()
    }

    /**
     * Delete any unsent crash reports.
     *
     * Useful for privacy compliance when user opts out.
     */
    fun deleteUnsentReports() {
        logger.info { "Deleting unsent crash reports" }
        crashReportingService.deleteUnsentReports()
    }

    /**
     * Check if the app crashed on the previous execution.
     *
     * @return true if there was a crash, false otherwise
     */
    fun didCrashOnPreviousExecution(): Boolean = crashReportingService.didCrashOnPreviousExecution()
}
