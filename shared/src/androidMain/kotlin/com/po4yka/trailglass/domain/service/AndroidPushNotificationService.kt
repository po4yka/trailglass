package com.po4yka.trailglass.domain.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * Android implementation of PushNotificationService using Firebase Cloud Messaging.
 *
 * This service provides push notification functionality for the Android platform.
 */
@Inject
class AndroidPushNotificationService(
    private val context: Context
) : PushNotificationService {
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance()

    override val tokenFlow: Flow<String> =
        callbackFlow {
            // Get initial token
            try {
                val token = firebaseMessaging.token.await()
                logger.debug { "Initial FCM token retrieved: ${token.take(10)}..." }
                trySend(token)
            } catch (e: Exception) {
                logger.error(e) { "Failed to get initial FCM token" }
            }

            // Note: Token refresh is handled by FirebaseMessagingService
            // which will emit new tokens via the repository/use case layer

            awaitClose {
                logger.debug { "Token flow closed" }
            }
        }

    override suspend fun getToken(): String? =
        try {
            val token = firebaseMessaging.token.await()
            logger.debug { "FCM token retrieved: ${token.take(10)}..." }
            token
        } catch (e: Exception) {
            logger.error(e) { "Failed to get FCM token" }
            null
        }

    override suspend fun requestNotificationPermission(): Boolean {
        // Android 13+ requires runtime permission for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if permission is already granted
            val isGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            logger.debug { "Notification permission status: $isGranted" }

            // On Android, permissions must be requested via Activity
            // Return false to indicate UI-based request is needed
            return isGranted
        }

        // Android 12 and below: notifications are auto-granted
        return true
    }

    override suspend fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            logger.debug { "Has notification permission: $hasPermission" }
            return hasPermission
        }
        // Android 12 and below: notifications are auto-granted
        return true
    }

    override suspend fun subscribeToTopic(topic: String) {
        try {
            firebaseMessaging.subscribeToTopic(topic).await()
            logger.info { "Subscribed to topic: $topic" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to subscribe to topic: $topic" }
        }
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        try {
            firebaseMessaging.unsubscribeFromTopic(topic).await()
            logger.info { "Unsubscribed from topic: $topic" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to unsubscribe from topic: $topic" }
        }
    }

    override suspend fun deleteToken() {
        try {
            firebaseMessaging.deleteToken().await()
            logger.info { "FCM token deleted" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete FCM token" }
        }
    }
}
