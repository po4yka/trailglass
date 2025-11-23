package com.po4yka.trailglass.feature.notifications

import com.po4yka.trailglass.data.repository.PushNotificationRepository
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for managing topic subscriptions.
 *
 * Topics allow you to send messages to multiple devices that have opted in to a particular topic.
 * Examples:
 * - "trip_updates" - Updates about trips
 * - "sync_complete" - Notifications when sync is complete
 * - "announcements" - App announcements
 */
@Inject
class ManageTopicSubscriptionUseCase(
    private val pushNotificationRepository: PushNotificationRepository
) {
    private val logger = logger()

    /**
     * Subscribe to a notification topic.
     *
     * @param topic The topic to subscribe to
     */
    suspend fun subscribe(topic: String) {
        logger.info { "Subscribing to topic: $topic" }
        pushNotificationRepository.subscribeToTopic(topic)
    }

    /**
     * Unsubscribe from a notification topic.
     *
     * @param topic The topic to unsubscribe from
     */
    suspend fun unsubscribe(topic: String) {
        logger.info { "Unsubscribing from topic: $topic" }
        pushNotificationRepository.unsubscribeFromTopic(topic)
    }
}
