package com.po4yka.trailglass.feature.notifications

import com.po4yka.trailglass.data.repository.PushNotificationRepository
import me.tatarka.inject.annotations.Inject

/**
 * Use case for requesting notification permission.
 */
@Inject
class RequestNotificationPermissionUseCase(
    private val pushNotificationRepository: PushNotificationRepository
) {
    /**
     * Request notification permission from the user.
     *
     * Platform-specific behavior:
     * - Android: Returns false on Android 12 and below (auto-granted), triggers permission dialog on Android 13+
     * - iOS: Triggers system permission dialog and returns result
     *
     * @return true if permissions were granted, false otherwise
     */
    suspend fun execute(): Boolean = pushNotificationRepository.requestNotificationPermission()

    /**
     * Check if notification permissions are granted.
     *
     * @return true if permissions are granted, false otherwise
     */
    suspend fun hasPermission(): Boolean = pushNotificationRepository.hasNotificationPermission()
}
