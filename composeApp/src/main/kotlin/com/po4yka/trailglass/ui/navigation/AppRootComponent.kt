package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.po4yka.trailglass.di.AppComponent
import kotlinx.serialization.Serializable

/** Top-level root component that manages navigation between authentication flow and the main application. */
interface AppRootComponent {
    /** Child stack for top-level navigation. */
    val childStack: Value<ChildStack<*, Child>>

    /** Navigate to authentication flow. */
    fun navigateToAuth()

    /** Navigate to main app (after successful authentication). */
    fun navigateToMain()

    /** Sealed class representing top-level navigation configurations. */
    @Serializable
    sealed interface Config {
        /** Authentication flow (Login, Register, ForgotPassword). */
        @Serializable
        data object Auth : Config

        /** Main application (Stats, Timeline, Map, Settings). */
        @Serializable
        data object Main : Config
    }

    /** Sealed class representing child components. */
    sealed class Child {
        data class Auth(
            val component: AuthRootComponent
        ) : Child()

        data class Main(
            val component: RootComponent
        ) : Child()
    }
}

/** Default implementation of AppRootComponent. */
class DefaultAppRootComponent(
    componentContext: ComponentContext,
    private val appComponent: AppComponent
) : AppRootComponent,
    ComponentContext by componentContext {
    private val navigation = StackNavigation<AppRootComponent.Config>()

    override val childStack: Value<ChildStack<*, AppRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = AppRootComponent.Config.serializer(),
            // Start with Auth flow - will check authentication status on initialization
            initialConfiguration = AppRootComponent.Config.Auth,
            handleBackButton = true,
            childFactory = ::createChild
        )

    override fun navigateToAuth() {
        navigation.replaceCurrent(AppRootComponent.Config.Auth)
    }

    override fun navigateToMain() {
        navigation.replaceCurrent(AppRootComponent.Config.Main)
    }

    private fun createChild(
        config: AppRootComponent.Config,
        componentContext: ComponentContext
    ): AppRootComponent.Child =
        when (config) {
            is AppRootComponent.Config.Auth ->
                AppRootComponent.Child.Auth(
                    component =
                        DefaultAuthRootComponent(
                            componentContext = componentContext,
                            authController = appComponent.authController,
                            onAuthenticated = ::navigateToMain
                        )
                )

            is AppRootComponent.Config.Main ->
                AppRootComponent.Child.Main(
                    component =
                        DefaultRootComponent(
                            componentContext = componentContext,
                            appComponent = appComponent
                        )
                )
        }
}
