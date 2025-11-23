package com.po4yka.trailglass.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.po4yka.trailglass.MainActivity
import com.po4yka.trailglass.R
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Firebase Cloud Messaging service for receiving push notifications.
 *
 * This service handles:
 * - New FCM token generation and refresh
 * - Incoming push notifications (foreground and background)
 * - Notification display and action handling
 *
 * The service is automatically invoked by Firebase when:
 * 1. A new FCM token is generated or refreshed
 * 2. A push notification is received
 */
class TrailGlassFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "trailglass_notifications"
        private const val NOTIFICATION_CHANNEL_NAME = "TrailGlass Notifications"
        private const val NOTIFICATION_ID = 2001
    }

    /**
     * Called when a new FCM token is generated.
     *
     * This happens when:
     * - The app is first installed
     * - The user reinstalls the app
     * - The user clears app data
     * - Firebase refreshes the token (periodically)
     *
     * @param token The new FCM registration token
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logger.info { "New FCM token received: ${token.take(10)}..." }

        // TODO: Send token to your backend server
        // This should be handled via the repository/use case layer
        // For now, just log it
        sendTokenToServer(token)
    }

    /**
     * Called when a message is received.
     *
     * This is called when:
     * - The app is in the foreground
     * - The app is in the background and the message is a data-only message (no notification payload)
     *
     * If the message has a notification payload and the app is in the background,
     * the system tray handles the notification automatically.
     *
     * @param remoteMessage The received message containing notification and/or data payload
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        logger.info { "Push notification received from: ${remoteMessage.from}" }

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            logger.debug { "Notification title: ${notification.title}" }
            logger.debug { "Notification body: ${notification.body}" }

            // Display notification
            showNotification(
                title = notification.title ?: "TrailGlass",
                message = notification.body ?: "",
                data = remoteMessage.data
            )
        }

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            logger.debug { "Data payload: ${remoteMessage.data}" }
            handleDataPayload(remoteMessage.data)
        }
    }

    /**
     * Called when the FCM service connection is deleted.
     *
     * This can happen when:
     * - The instance ID is invalidated
     * - The app is uninstalled
     * - The app data is cleared
     */
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        logger.warn { "FCM messages deleted - possible token refresh needed" }
    }

    /**
     * Send the FCM token to your backend server.
     *
     * TODO: Implement actual server communication via repository/use case
     */
    private fun sendTokenToServer(token: String) {
        // TODO: Implement via repository/use case to send token to backend
        logger.debug { "Token should be sent to server: ${token.take(10)}..." }
    }

    /**
     * Handle data payload from push notification.
     *
     * Data messages can trigger actions in the app, such as:
     * - Syncing data
     * - Opening specific screens
     * - Updating local cache
     */
    private fun handleDataPayload(data: Map<String, String>) {
        // TODO: Implement custom data handling based on your app's needs
        // Examples:
        // - "action" -> "sync": Trigger data sync
        // - "action" -> "open_trip": Open specific trip detail screen
        // - "action" -> "refresh": Refresh timeline

        val action = data["action"]
        logger.info { "Handling data action: $action" }

        when (action) {
            "sync" -> {
                // TODO: Trigger background sync
                logger.debug { "Sync action received" }
            }
            "open_trip" -> {
                val tripId = data["trip_id"]
                logger.debug { "Open trip action received for trip: $tripId" }
                // TODO: Navigate to trip detail
            }
            else -> {
                logger.debug { "Unknown action: $action" }
            }
        }
    }

    /**
     * Display a notification in the system tray.
     *
     * @param title Notification title
     * @param message Notification message body
     * @param data Additional data from the push notification
     */
    private fun showNotification(
        title: String,
        message: String,
        data: Map<String, String>
    ) {
        createNotificationChannel()

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // Add data as extras if needed
                data.forEach { (key, value) ->
                    putExtra(key, value)
                }
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val notification =
            NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        logger.debug { "Notification displayed: $title" }
    }

    /**
     * Create notification channel for Android 8.0+ (API 26+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Push notifications from TrailGlass"
                    enableLights(true)
                    enableVibration(true)
                }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
