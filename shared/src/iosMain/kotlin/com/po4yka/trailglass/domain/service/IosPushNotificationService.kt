package com.po4yka.trailglass.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Inject
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val logger = KotlinLogging.logger {}

/**
 * iOS implementation of PushNotificationService using Firebase Cloud Messaging and APNs.
 *
 * This service provides push notification functionality for the iOS platform.
 *
 * Note: Firebase iOS SDK integration requires CocoaPods configuration.
 * The actual Firebase setup must be done in the iosApp's AppDelegate or SceneDelegate.
 * See the iOS app code for Firebase initialization.
 */
@OptIn(ExperimentalForeignApi::class)
@Inject
class IosPushNotificationService : PushNotificationService {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    // Note: Firebase token handling needs to be implemented via Swift/Objective-C interop
    // For now, this is a placeholder that should be connected to Firebase iOS SDK
    override val tokenFlow: Flow<String> =
        flow {
            logger.warn { "iOS FCM token flow not yet implemented - requires Firebase iOS SDK integration" }
            // TODO: Implement Firebase token flow via expect/actual or callback from Swift
        }

    override suspend fun getToken(): String? {
        logger.warn { "iOS FCM token retrieval not yet implemented - requires Firebase iOS SDK integration" }
        // TODO: Implement via expect/actual or callback from Swift to get Firebase token
        // For now, return null
        return null
    }

    override suspend fun requestNotificationPermission(): Boolean =
        suspendCoroutine { continuation ->
            val options =
                UNAuthorizationOptionAlert or
                    UNAuthorizationOptionSound or
                    UNAuthorizationOptionBadge

            notificationCenter.requestAuthorizationWithOptions(
                options
            ) { granted, error ->
                if (error != null) {
                    logger.error { "Failed to request notification permission: ${error.localizedDescription}" }
                    continuation.resume(false)
                } else {
                    logger.info { "Notification permission granted: $granted" }
                    continuation.resume(granted)
                }
            }
        }

    override suspend fun hasNotificationPermission(): Boolean =
        suspendCoroutine { continuation ->
            notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
                val authorizationStatus = settings?.authorizationStatus
                val hasPermission =
                    authorizationStatus == UNAuthorizationStatusAuthorized ||
                        authorizationStatus == UNAuthorizationStatusProvisional

                logger.debug { "Notification permission status: $authorizationStatus" }
                continuation.resume(hasPermission)
            }
        }

    override suspend fun subscribeToTopic(topic: String) {
        logger.warn { "iOS topic subscription not yet implemented - requires Firebase iOS SDK integration" }
        // TODO: Implement via Firebase iOS SDK
        // Firebase.Messaging.messaging().subscribe(toTopic: topic)
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        logger.warn { "iOS topic unsubscription not yet implemented - requires Firebase iOS SDK integration" }
        // TODO: Implement via Firebase iOS SDK
        // Firebase.Messaging.messaging().unsubscribe(fromTopic: topic)
    }

    override suspend fun deleteToken() {
        logger.warn { "iOS token deletion not yet implemented - requires Firebase iOS SDK integration" }
        // TODO: Implement via Firebase iOS SDK
        // Firebase.Messaging.messaging().deleteToken()
    }
}
