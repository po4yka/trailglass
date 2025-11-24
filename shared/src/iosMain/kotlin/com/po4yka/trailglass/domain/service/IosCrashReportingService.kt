package com.po4yka.trailglass.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * iOS implementation of CrashReportingService using Firebase Crashlytics.
 *
 * This service provides crash reporting and error tracking for the iOS platform.
 *
 * **Implementation Note:**
 * Firebase Crashlytics iOS SDK requires Swift/Objective-C interop as it's not directly
 * accessible from Kotlin/Native. To complete the implementation:
 *
 * 1. Create a Swift helper class `FirebaseCrashlyticsHelper` in the iOS app:
 *    ```swift
 *    import FirebaseCrashlytics
 *    @objc class FirebaseCrashlyticsHelper: NSObject {
 *        @objc static func recordException(_ error: NSError) {
 *            Crashlytics.crashlytics().record(error: error)
 *        }
 *        @objc static func log(_ message: String) {
 *            Crashlytics.crashlytics().log(message)
 *        }
 *        // ... other methods
 *    }
 *    ```
 *
 * 2. Use cinterop or expect/actual pattern to call Swift from Kotlin
 * 3. Or use @ObjCName annotations for direct interop
 *
 * For now, methods log warnings and are stubbed. They will work once Swift helpers are created.
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
