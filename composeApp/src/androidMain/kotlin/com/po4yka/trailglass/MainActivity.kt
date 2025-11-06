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
import com.po4yka.trailglass.ui.navigation.DefaultRootComponent
import com.po4yka.trailglass.ui.navigation.RootComponent

class MainActivity : ComponentActivity() {

    /**
     * Access to the application-level DI component.
     * Use this to get dependencies like controllers and repositories.
     */
    private val appComponent: AppComponent by lazy {
        (application as TrailGlassApplication).appComponent
    }

    private lateinit var rootComponent: RootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Create the root component with Decompose lifecycle integration
        rootComponent = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            appComponent = appComponent
        )

        // Handle deep link if present in launch intent
        handleIntent(intent)

        setContent {
            App(
                rootComponent = rootComponent,
                networkConnectivityMonitor = appComponent.networkConnectivityMonitor
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep links when app is already running
        handleIntent(intent)
    }

    /**
     * Handle deep link intents.
     * Supports both trailglass:// and https://trailglass.app URLs.
     */
    private fun handleIntent(intent: Intent) {
        val data = intent.data ?: return

        // Extract path from the URI
        // For trailglass://app/stats -> path = "stats"
        // For https://trailglass.app/timeline -> path = "timeline"
        val path = data.path?.removePrefix("/") ?: data.host ?: return

        rootComponent.handleDeepLink(path)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Note: Preview doesn't have access to appComponent,
    // so this preview won't work with the actual navigation
    // For proper previews, preview individual screens instead
}
