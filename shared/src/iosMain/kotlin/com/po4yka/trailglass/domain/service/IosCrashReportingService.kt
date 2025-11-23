package com.po4yka.trailglass.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * iOS implementation of CrashReportingService using Firebase Crashlytics.
 *
 * This service provides crash reporting and error tracking for the iOS platform.
 *
 * Note: Firebase iOS SDK integration requires CocoaPods configuration.
 * The actual Firebase setup must be done in the iosApp's AppDelegate.
 * See the iOS app code for Firebase initialization.
 */
@Inject
class IosCrashReportingService : CrashReportingService {
    override fun recordException(throwable: Throwable) {
        logger.warn { "iOS Crashlytics recordException not yet implemented - requires Firebase iOS SDK integration" }
        logger.debug { "Exception: ${throwable.message}" }
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().record(error: error)
    }

    override fun log(message: String) {
        logger.debug { "Crashlytics log: $message" }
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().log(message)
    }

    override fun setCustomKey(
        key: String,
        value: String
    ) {
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    override fun setCustomKey(
        key: String,
        value: Int
    ) {
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    override fun setCustomKey(
        key: String,
        value: Boolean
    ) {
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    override fun setCustomKey(
        key: String,
        value: Float
    ) {
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    override fun setCustomKey(
        key: String,
        value: Double
    ) {
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    override fun setCustomKey(
        key: String,
        value: Long
    ) {
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    override fun setUserId(userId: String) {
        logger.debug { "Setting Crashlytics user ID: $userId" }
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setUserID(userId)
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        logger.info { "Crashlytics collection enabled: $enabled" }
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
    }

    override fun isCrashlyticsCollectionEnabled(): Boolean {
        logger.warn { "iOS isCrashlyticsCollectionEnabled not yet implemented" }
        // TODO: Implement via Firebase iOS SDK
        return false
    }

    override fun sendUnsentReports() {
        logger.info { "Sending unsent crash reports" }
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().sendUnsentReports()
    }

    override fun deleteUnsentReports() {
        logger.info { "Deleting unsent crash reports" }
        // TODO: Implement via Firebase iOS SDK
        // Crashlytics.crashlytics().deleteUnsentReports()
    }

    override fun didCrashOnPreviousExecution(): Boolean {
        logger.warn { "iOS didCrashOnPreviousExecution not yet implemented" }
        // TODO: Implement via Firebase iOS SDK
        // return Crashlytics.crashlytics().didCrashDuringPreviousExecution()
        return false
    }
}
