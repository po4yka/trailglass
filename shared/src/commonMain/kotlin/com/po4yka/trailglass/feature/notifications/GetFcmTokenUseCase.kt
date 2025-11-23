package com.po4yka.trailglass.feature.notifications

import com.po4yka.trailglass.data.repository.PushNotificationRepository
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

/**
 * Use case for getting the FCM token.
 */
@Inject
class GetFcmTokenUseCase(
    private val pushNotificationRepository: PushNotificationRepository
) {
    /**
     * Get the current FCM token.
     *
     * @return The current FCM token, or null if not available
     */
    suspend fun execute(): String? = pushNotificationRepository.getToken()

    /**
     * Get the FCM token as a Flow.
     *
     * @return Flow of FCM token updates
     */
    fun executeFlow(): Flow<String?> = pushNotificationRepository.getTokenFlow()
}
