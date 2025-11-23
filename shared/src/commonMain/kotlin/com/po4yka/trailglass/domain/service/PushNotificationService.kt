package com.po4yka.trailglass.domain.service

import kotlinx.coroutines.flow.Flow

/**
 * Service for managing push notifications.
 *
 * This is a platform-specific service that handles Firebase Cloud Messaging (FCM) for push notifications.
 * Platform implementations:
 * - Android: Firebase Cloud Messaging (FCM)
 * - iOS: Firebase Cloud Messaging with APNs
 */
interface PushNotificationService {
    /**
     * Flow of FCM tokens.
     *
     * Emits new tokens when they are refreshed by the platform.
     */
    val tokenFlow: Flow<String>

    /**
     * Get the current FCM token.
     *
     * @return The current FCM token, or null if not available
     */
    suspend fun getToken(): String?

    /**
     * Request notification permission from the user.
     *
     * Platform-specific behavior:
     * - Android: Returns false on Android 12 and below (auto-granted), triggers permission dialog on Android 13+
     * - iOS: Triggers system permission dialog and returns result
     *
     * @return true if permissions were granted, false otherwise or if platform requires UI-based request
     */
    suspend fun requestNotificationPermission(): Boolean

    /**
     * Check if notification permissions are granted.
     *
     * @return true if permissions are granted, false otherwise
     */
    suspend fun hasNotificationPermission(): Boolean

    /**
     * Subscribe to a notification topic.
     *
     * @param topic The topic to subscribe to
     */
    suspend fun subscribeToTopic(topic: String)

    /**
     * Unsubscribe from a notification topic.
     *
     * @param topic The topic to unsubscribe from
     */
    suspend fun unsubscribeFromTopic(topic: String)

    /**
     * Delete the FCM token.
     *
     * Use this when the user logs out to prevent receiving notifications.
     */
    suspend fun deleteToken()
}
