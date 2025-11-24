package com.po4yka.trailglass.di

import me.tatarka.inject.annotations.Component

/**
 * iOS-specific AppComponent that includes iOS-specific modules.
 */
@AppScope
@Component
abstract class IOSAppComponent(
    @Component override val platformModule: PlatformModule
) : AppComponent(platformModule)
