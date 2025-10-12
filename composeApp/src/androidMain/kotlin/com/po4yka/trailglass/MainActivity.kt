package com.po4yka.trailglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.po4yka.trailglass.di.AppComponent

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

        // Example: Access dependencies through the component
        // val timelineController = appComponent.timelineController
        // val statsController = appComponent.statsController
        // val locationRepository = appComponent.locationRepository

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}