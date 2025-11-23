package com.po4yka.trailglass.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing push notifications and FCM tokens.
 */
interface PushNotificationRepository {
    /**
     * Get the current FCM token.
     *
     * @return The current FCM token, or null if not available
     */
    suspend fun getToken(): String?

    /**
     * Save the FCM token to local storage.
     *
     * @param token The FCM token to save
     */
    suspend fun saveToken(token: String)

    /**
     * Get the FCM token as a Flow.
     *
     * @return Flow of FCM token updates
     */
    fun getTokenFlow(): Flow<String?>

    /**
     * Request notification permission from the user.
     *
     * @return true if permissions were granted, false otherwise
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

    /**
     * Clear the locally stored FCM token.
     */
    suspend fun clearStoredToken()
}
