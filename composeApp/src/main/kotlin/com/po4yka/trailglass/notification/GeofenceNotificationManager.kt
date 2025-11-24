package com.po4yka.trailglass.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.po4yka.trailglass.MainActivity
import com.po4yka.trailglass.R
import com.po4yka.trailglass.domain.model.Region

/**
 * Manager for displaying geofence-related notifications.
 * Handles notifications for region enter and exit events.
 */
class GeofenceNotificationManager(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Show notification when user enters a region.
     */
    fun showRegionEnterNotification(region: Region) {
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Entered ${region.name}")
                .setContentText(region.description ?: "You have entered a tracked region")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_LOCATION)
                .setAutoCancel(true)
                .setContentIntent(createMapIntent(region))
                .addAction(
                    android.R.drawable.ic_menu_mapmode,
                    "View on Map",
                    createMapIntent(region)
                ).build()

        notificationManager.notify(generateNotificationId(region.id), notification)
    }

    /**
     * Show notification when user exits a region.
     */
    fun showRegionExitNotification(region: Region) {
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Left ${region.name}")
                .setContentText(region.description ?: "You have left a tracked region")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_LOCATION)
                .setAutoCancel(true)
                .setContentIntent(createMapIntent(region))
                .addAction(
                    android.R.drawable.ic_menu_mapmode,
                    "View on Map",
                    createMapIntent(region)
                ).build()

        notificationManager.notify(generateNotificationId(region.id), notification)
    }

    /**
     * Create a PendingIntent to open the app and show the region on the map.
     */
    private fun createMapIntent(region: Region): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_REGION_ID, region.id)
                putExtra(EXTRA_REGION_LAT, region.latitude)
                putExtra(EXTRA_REGION_LNG, region.longitude)
                putExtra(EXTRA_SHOW_ON_MAP, true)
            }

        return PendingIntent.getActivity(
            context,
            generateNotificationId(region.id),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Create the notification channel for geofence notifications.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Generate a unique notification ID for a region.
     * This ensures each region's notifications can be managed independently.
     */
    private fun generateNotificationId(regionId: String): Int =
        (NOTIFICATION_ID_BASE + regionId.hashCode()).coerceIn(
            NOTIFICATION_ID_BASE,
            NOTIFICATION_ID_MAX
        )

    /**
     * Cancel a notification for a specific region.
     */
    fun cancelNotification(regionId: String) {
        notificationManager.cancel(generateNotificationId(regionId))
    }

    /**
     * Cancel all geofence notifications.
     */
    fun cancelAllNotifications() {
        // Note: This cancels all notifications in the channel
        // If more granular control is needed, track notification IDs
        notificationManager.cancel(NOTIFICATION_ID_BASE)
    }

    companion object {
        private const val CHANNEL_ID = "geofence_notifications"
        private const val CHANNEL_NAME = "Region Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for entering and exiting tracked regions"

        private const val NOTIFICATION_ID_BASE = 2000
        private const val NOTIFICATION_ID_MAX = 2999

        // Intent extras for opening the map
        const val EXTRA_REGION_ID = "region_id"
        const val EXTRA_REGION_LAT = "region_lat"
        const val EXTRA_REGION_LNG = "region_lng"
        const val EXTRA_SHOW_ON_MAP = "show_on_map"
    }
}
