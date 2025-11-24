package com.po4yka.trailglass.di

/**
 * Creates the iOS application DI component.
 *
 * Usage:
 * ```swift
 * lazy var appComponent: AppComponent = {
 *     return CreateKt.createIOSAppComponent()
 * }()
 * ```
 *
 * @return Fully configured AppComponent instance
 */
fun createIOSAppComponent(): AppComponent {
    val platformModule = IOSPlatformModule()
    return AppComponent::class.create(platformModule)
}
