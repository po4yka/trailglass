package com.po4yka.trailglass.di

import android.content.Context
import com.po4yka.trailglass.location.AndroidCurrentLocationProvider
import com.po4yka.trailglass.location.CurrentLocationProvider
import me.tatarka.inject.annotations.Provides

/**
 * Android-specific location bindings.
 */
interface AndroidLocationModule {
    /**
     * Provides Android implementation of CurrentLocationProvider.
     */
    @AppScope
    @Provides
    fun provideCurrentLocationProvider(@ApplicationContext context: Context): CurrentLocationProvider =
        AndroidCurrentLocationProvider(context)
}
