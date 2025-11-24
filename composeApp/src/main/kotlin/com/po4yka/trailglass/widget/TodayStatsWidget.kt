package com.po4yka.trailglass.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.po4yka.trailglass.MainActivity
import com.po4yka.trailglass.R
import com.po4yka.trailglass.TrailGlassApplication
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

/**
 * Widget provider for displaying today's travel stats.
 *
 * Shows:
 * - Distance traveled today
 * - Number of places visited
 * - Current tracking status
 */
class TodayStatsWidget : AppWidgetProvider() {
    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        logger.debug { "Updating widget for ${appWidgetIds.size} instances" }

        // Update each widget instance
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        logger.info { "Widget enabled" }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        logger.info { "Widget disabled" }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        widgetScope.launch {
            try {
                // Get widget data
                val widgetData = getWidgetData(context)

                // Create RemoteViews
                val views = RemoteViews(context.packageName, R.layout.widget_today_stats)

                // Update distance
                views.setTextViewText(
                    R.id.widget_distance,
                    context.getString(R.string.widget_distance_km, widgetData.distanceKm)
                )

                // Update places
                views.setTextViewText(
                    R.id.widget_places,
                    context.getString(R.string.widget_places, widgetData.placesCount)
                )

                // Update status
                val statusText =
                    if (widgetData.isTracking) {
                        context.getString(R.string.widget_tracking_active)
                    } else {
                        context.getString(R.string.widget_tracking_inactive)
                    }
                views.setTextViewText(R.id.widget_status, statusText)

                // Set up click intent to open app
                val intent =
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        data = android.net.Uri.parse("trailglass://app/stats/today")
                    }
                val pendingIntent =
                    PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)

                logger.debug { "Widget updated successfully: distance=${widgetData.distanceKm}km, places=${widgetData.placesCount}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update widget: ${e.message}" }
            }
        }
    }

    private suspend fun getWidgetData(context: Context): WidgetData {
        return withContext(Dispatchers.IO) {
            try {
                val application = context.applicationContext as? TrailGlassApplication
                if (application == null) {
                    logger.warn { "Application is not TrailGlassApplication, returning default data" }
                    return@withContext WidgetData(
                        distanceKm = 0.0,
                        placesCount = 0,
                        isTracking = false
                    )
                }

                // Get widget state repository from DI
                val widgetStateRepository = application.appComponent.widgetStateRepository

                // Get today's stats snapshot
                val stats = widgetStateRepository.getTodayStatsSnapshot()

                WidgetData(
                    distanceKm = stats.distanceKm,
                    placesCount = stats.placesVisited,
                    isTracking = stats.isTracking
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to get widget data: ${e.message}" }
                // Return default data on error
                WidgetData(
                    distanceKm = 0.0,
                    placesCount = 0,
                    isTracking = false
                )
            }
        }
    }

    companion object {
        /**
         * Request widget update from external components.
         *
         * Call this when location data changes to refresh the widget.
         */
        fun requestUpdate(context: Context) {
            val intent =
                Intent(context, TodayStatsWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                }
            val ids =
                AppWidgetManager.getInstance(context).getAppWidgetIds(
                    android.content.ComponentName(context, TodayStatsWidget::class.java)
                )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}

/**
 * Data class for widget display.
 */
data class WidgetData(
    val distanceKm: Double,
    val placesCount: Int,
    val isTracking: Boolean
)
