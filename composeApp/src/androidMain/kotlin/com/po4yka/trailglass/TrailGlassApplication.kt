package com.po4yka.trailglass

import android.app.Application
import com.po4yka.trailglass.di.AppComponent
import com.po4yka.trailglass.di.createAndroidAppComponent

/**
 * TrailGlass Application class.
 *
 * Initializes the dependency injection component and provides
 * application-wide dependencies.
 */
class TrailGlassApplication : Application() {

    /**
     * Application-level DI component.
     * Provides all application dependencies (repositories, controllers, etc.)
     */
    val appComponent: AppComponent by lazy {
        createAndroidAppComponent(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Component is initialized lazily on first access
        // You can perform other initialization here if needed
    }
}
