package com.po4yka.trailglass.domain.service

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * Android implementation of CrashReportingService using Firebase Crashlytics.
 *
 * This service provides crash reporting and error tracking for the Android platform.
 */
@Inject
class AndroidCrashReportingService : CrashReportingService {
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    override fun recordException(throwable: Throwable) {
        logger.debug { "Recording non-fatal exception: ${throwable.message}" }
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        logger.debug { "Crashlytics log: $message" }
        crashlytics.log(message)
    }

    override fun setCustomKeyString(
        key: String,
        value: String
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKeyInt(
        key: String,
        value: Int
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKeyBool(
        key: String,
        value: Boolean
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKeyFloat(
        key: String,
        value: Float
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKeyDouble(
        key: String,
        value: Double
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKeyLong(
        key: String,
        value: Long
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setUserId(userId: String) {
        logger.debug { "Setting Crashlytics user ID: $userId" }
        crashlytics.setUserId(userId)
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        logger.info { "Crashlytics collection enabled: $enabled" }
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    override fun isCrashlyticsCollectionEnabled(): Boolean = crashlytics.isCrashlyticsCollectionEnabled

    override fun sendUnsentReports() {
        logger.info { "Sending unsent crash reports" }
        crashlytics.sendUnsentReports()
    }

    override fun deleteUnsentReports() {
        logger.info { "Deleting unsent crash reports" }
        crashlytics.deleteUnsentReports()
    }

    override fun didCrashOnPreviousExecution(): Boolean {
        val didCrash = crashlytics.didCrashOnPreviousExecution()
        logger.debug { "Did crash on previous execution: $didCrash" }
        return didCrash
    }
}
