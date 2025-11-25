package com.po4yka.trailglass

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.defaultComponentContext
import com.po4yka.trailglass.di.AppComponent
import com.po4yka.trailglass.location.tracking.TrackingMode
import com.po4yka.trailglass.ui.navigation.AppRootComponent
import com.po4yka.trailglass.ui.navigation.DefaultAppRootComponent
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class MainActivity : ComponentActivity() {
    /** Access to the application-level DI component. Use this to get dependencies like controllers and repositories. */
    private val appComponent: AppComponent by lazy {
        (application as TrailGlassApplication).appComponent
    }

    private lateinit var appRootComponent: AppRootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Create the app root component with authentication support
        appRootComponent =
            DefaultAppRootComponent(
                componentContext = defaultComponentContext(),
                appComponent = appComponent
            )

        // Handle deep link if present in launch intent
        handleIntent(intent)

        setContent {
            App(
                appRootComponent = appRootComponent,
                networkConnectivityMonitor = appComponent.networkConnectivityMonitor
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep links when app is already running
        handleIntent(intent)
    }

    /** Handle deep link intents. Supports both trailglass:// and https://trailglass.app URLs. */
    private fun handleIntent(intent: Intent) {
        val data = intent.data ?: return

        logger.info { "Handling deep link: $data" }

        // Extract path from the URI
        // For trailglass://app/tracking/start -> host = "app", path = "/tracking/start"
        // For https://trailglass.app/timeline -> host = "trailglass.app", path = "/timeline"
        val path = data.path?.removePrefix("/") ?: return

        logger.debug { "Deep link path: $path" }

        // Route to appropriate screen based on path
        when {
            // Tracking shortcuts
            path.startsWith("tracking/start") -> {
                logger.info { "Starting tracking from shortcut" }
                appComponent.locationTrackingController.startTracking(TrackingMode.ACTIVE)
                appRootComponent.handleDeepLink("map")
            }

            // Stats shortcuts
            path.startsWith("stats/today") -> {
                logger.info { "Opening today's stats from shortcut/widget" }
                appRootComponent.handleDeepLink("stats")
            }

            // Export shortcuts
            path.startsWith("export/recent") -> {
                logger.info { "Opening export from shortcut" }
                appRootComponent.handleDeepLink("settings")
            }

            // Timeline shortcut
            path == "timeline" -> {
                logger.info { "Opening timeline from shortcut" }
                appRootComponent.handleDeepLink("timeline")
            }

            else -> {
                logger.warn { "Unknown deep link path: $path" }
            }
        }

        // Note: Deep links only work properly when user is authenticated and in main app
        // You may need to queue the navigation if user is not yet authenticated
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Note: Preview doesn't have access to appComponent,
    // so this preview won't work with the actual navigation
    // For proper previews, preview individual screens instead
}
