package com.po4yka.trailglass.data.repository.impl

import com.po4yka.trailglass.data.repository.PushNotificationRepository
import com.po4yka.trailglass.domain.service.PushNotificationService
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

/**
 * Implementation of PushNotificationRepository.
 *
 * This repository coordinates between the platform-specific PushNotificationService
 * and the rest of the application.
 */
@Inject
class PushNotificationRepositoryImpl(
    private val pushNotificationService: PushNotificationService
) : PushNotificationRepository {
    private val logger = logger()

    // In-memory token storage (could be persisted to DataStore/UserDefaults)
    private val _tokenFlow = MutableStateFlow<String?>(null)

    override suspend fun getToken(): String? {
        val token = pushNotificationService.getToken()
        if (token != null) {
            _tokenFlow.value = token
        }
        return token
    }

    override suspend fun saveToken(token: String) {
        logger.info { "Saving FCM token: ${token.take(10)}..." }
        _tokenFlow.value = token
        // TODO: Persist token to DataStore/UserDefaults for offline access
    }

    override fun getTokenFlow(): Flow<String?> = _tokenFlow.asStateFlow()

    override suspend fun requestNotificationPermission(): Boolean {
        logger.info { "Requesting notification permission" }
        return pushNotificationService.requestNotificationPermission()
    }

    override suspend fun hasNotificationPermission(): Boolean = pushNotificationService.hasNotificationPermission()

    override suspend fun subscribeToTopic(topic: String) {
        logger.info { "Subscribing to topic: $topic" }
        pushNotificationService.subscribeToTopic(topic)
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        logger.info { "Unsubscribing from topic: $topic" }
        pushNotificationService.unsubscribeFromTopic(topic)
    }

    override suspend fun deleteToken() {
        logger.info { "Deleting FCM token" }
        pushNotificationService.deleteToken()
        _tokenFlow.value = null
    }

    override suspend fun clearStoredToken() {
        logger.info { "Clearing stored FCM token" }
        _tokenFlow.value = null
        // TODO: Clear from DataStore/UserDefaults
    }
}
