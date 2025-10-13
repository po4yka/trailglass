package com.po4yka.trailglass

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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Create the root component with Decompose lifecycle integration
        val rootComponent: RootComponent = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            appComponent = appComponent
        )

        setContent {
            App(rootComponent = rootComponent)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Note: Preview doesn't have access to appComponent,
    // so this preview won't work with the actual navigation
    // For proper previews, preview individual screens instead
}
