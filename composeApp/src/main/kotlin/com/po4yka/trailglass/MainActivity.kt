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
import com.po4yka.trailglass.ui.navigation.AppRootComponent
import com.po4yka.trailglass.ui.navigation.DefaultAppRootComponent

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

        // Extract path from the URI
        // For trailglass://app/stats -> path = "stats"
        // For https://trailglass.app/timeline -> path = "timeline"
        data.path?.removePrefix("/") ?: data.host ?: return

        // Deep links only work when user is authenticated and in main app
        // TODO: Handle deep linking to main screens once authenticated
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Note: Preview doesn't have access to appComponent,
    // so this preview won't work with the actual navigation
    // For proper previews, preview individual screens instead
}
