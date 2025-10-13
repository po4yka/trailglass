package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.po4yka.trailglass.di.AppComponent
import kotlinx.serialization.Serializable

/**
 * Root component for the application navigation.
 * Manages the main navigation stack with bottom navigation screens.
 */
interface RootComponent {
    /**
     * Child stack representing the navigation stack.
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Navigate to a specific screen.
     */
    fun navigateToScreen(config: Config)

    /**
     * Sealed class representing the navigation configurations.
     */
    @Serializable
    sealed interface Config {
        @Serializable
        data object Stats : Config

        @Serializable
        data object Timeline : Config

        @Serializable
        data object Map : Config

        @Serializable
        data object Settings : Config
    }

    /**
     * Sealed class representing the child components.
     */
    sealed class Child {
        data class Stats(val component: StatsComponent) : Child()
        data class Timeline(val component: TimelineComponent) : Child()
        data class Map(val component: MapComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
    }
}

/**
 * Default implementation of RootComponent.
 */
class DefaultRootComponent(
    componentContext: ComponentContext,
    private val appComponent: AppComponent
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<RootComponent.Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = RootComponent.Config.serializer(),
        initialConfiguration = RootComponent.Config.Stats,
        handleBackButton = true,
        childFactory = ::createChild
    )

    override fun navigateToScreen(config: RootComponent.Config) {
        navigation.replaceAll(config)
    }

    private fun createChild(
        config: RootComponent.Config,
        componentContext: ComponentContext
    ): RootComponent.Child = when (config) {
        is RootComponent.Config.Stats -> RootComponent.Child.Stats(
            component = DefaultStatsComponent(
                componentContext = componentContext,
                statsController = appComponent.statsController
            )
        )

        is RootComponent.Config.Timeline -> RootComponent.Child.Timeline(
            component = DefaultTimelineComponent(
                componentContext = componentContext,
                timelineController = appComponent.timelineController
            )
        )

        is RootComponent.Config.Map -> RootComponent.Child.Map(
            component = DefaultMapComponent(
                componentContext = componentContext,
                mapController = appComponent.mapController
            )
        )

        is RootComponent.Config.Settings -> RootComponent.Child.Settings(
            component = DefaultSettingsComponent(
                componentContext = componentContext,
                locationTrackingController = appComponent.locationTrackingController
            )
        )
    }
}
