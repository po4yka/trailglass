package com.po4yka.trailglass.feature.crashreporting

import com.po4yka.trailglass.domain.service.CrashReportingService
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for recording non-fatal exceptions to Crashlytics.
 *
 * Use this to log exceptions that are caught and handled but still indicate
 * something unexpected happened.
 */
@Inject
class RecordExceptionUseCase(
    private val crashReportingService: CrashReportingService
) {
    private val logger = logger()

    /**
     * Record a non-fatal exception.
     *
     * @param throwable The exception to log
     * @param additionalContext Optional map of additional context to log
     */
    fun execute(
        throwable: Throwable,
        additionalContext: Map<String, String> = emptyMap()
    ) {
        logger.debug { "Recording exception: ${throwable.message}" }

        // Add additional context as custom keys
        additionalContext.forEach { (key, value) ->
            crashReportingService.setCustomKey(key, value)
        }

        // Record the exception
        crashReportingService.recordException(throwable)
    }

    /**
     * Record an exception with a custom message and additional context.
     *
     * @param message Custom log message
     * @param throwable The exception to log
     * @param additionalContext Optional map of additional context
     */
    fun executeWithMessage(
        message: String,
        throwable: Throwable,
        additionalContext: Map<String, String> = emptyMap()
    ) {
        logger.debug { message }
        crashReportingService.log(message)
        execute(throwable, additionalContext)
    }
}
