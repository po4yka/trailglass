package com.po4yka.trailglass.di

import android.content.Context

/**
 * Creates the Android application DI component.
 *
 * Usage:
 * ```kotlin
 * class MyApplication : Application() {
 *     val appComponent by lazy {
 *         createAndroidAppComponent(applicationContext)
 *     }
 * }
 * ```
 *
 * @param context Android application context
 * @return Fully configured AppComponent instance
 */
fun createAndroidAppComponent(context: Context): AppComponent {
    val platformModule = AndroidPlatformModule(context)
    return AppComponent::class.create(platformModule)
}
