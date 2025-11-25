package com.po4yka.trailglass.feature.crashreporting

import com.po4yka.trailglass.domain.service.CrashReportingService
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for setting user information in Crashlytics.
 *
 * This helps identify and filter crashes by user.
 */
@Inject
class SetCrashReportingUserUseCase(
    private val crashReportingService: CrashReportingService
) {
    private val logger = logger()

    /**
     * Set the user identifier for crash reporting.
     *
     * @param userId The user identifier
     * @param customAttributes Optional map of custom user attributes
     */
    fun execute(
        userId: String,
        customAttributes: Map<String, String> = emptyMap()
    ) {
        logger.info { "Setting crash reporting user: $userId" }

        // Set user ID
        crashReportingService.setUserId(userId)

        // Set custom user attributes
        customAttributes.forEach { (key, value) ->
            crashReportingService.setCustomKeyString("user_$key", value)
        }
    }

    /**
     * Clear the user identifier (e.g., on logout).
     */
    fun clearUser() {
        logger.info { "Clearing crash reporting user" }
        crashReportingService.setUserId("")
    }
}
