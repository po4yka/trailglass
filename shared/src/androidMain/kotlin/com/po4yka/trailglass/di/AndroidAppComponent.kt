package com.po4yka.trailglass.di

import me.tatarka.inject.annotations.Component

/**
 * Android-specific AppComponent that includes Android-specific modules.
 */
@AppScope
@Component
abstract class AndroidAppComponent(
    @Component override val platformModule: PlatformModule
) : AppComponent(platformModule), AndroidLocationModule
