package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.po4yka.trailglass.feature.auth.AuthController
import kotlinx.serialization.Serializable

/** Root component for authentication flow. Manages navigation between Login, Register, and ForgotPassword screens. */
interface AuthRootComponent {
    /** Child stack for auth navigation. */
    val childStack: Value<ChildStack<*, Child>>

    /** Navigate to register screen. */
    fun navigateToRegister()

    /** Navigate to forgot password screen. */
    fun navigateToForgotPassword()

    /** Navigate back. */
    fun navigateBack()

    /** Callback when user successfully authenticates. */
    val onAuthenticated: () -> Unit

    /** Sealed class representing auth navigation configurations. */
    @Serializable
    sealed interface Config {
        @Serializable
        data object Login : Config

        @Serializable
        data object Register : Config

        @Serializable
        data object ForgotPassword : Config
    }

    /** Sealed class representing child components. */
    sealed class Child {
        data class Login(
            val component: LoginComponent
        ) : Child()

        data class Register(
            val component: RegisterComponent
        ) : Child()

        data class ForgotPassword(
            val component: ForgotPasswordComponent
        ) : Child()
    }
}

/** Default implementation of AuthRootComponent. */
class DefaultAuthRootComponent(
    componentContext: ComponentContext,
    private val authController: AuthController,
    override val onAuthenticated: () -> Unit
) : AuthRootComponent,
    ComponentContext by componentContext {
    private val navigation = StackNavigation<AuthRootComponent.Config>()

    override val childStack: Value<ChildStack<*, AuthRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = AuthRootComponent.Config.serializer(),
            initialConfiguration = AuthRootComponent.Config.Login,
            handleBackButton = true,
            childFactory = ::createChild
        )

    @OptIn(DelicateDecomposeApi::class)
    override fun navigateToRegister() {
        navigation.push(AuthRootComponent.Config.Register)
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun navigateToForgotPassword() {
        navigation.push(AuthRootComponent.Config.ForgotPassword)
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun navigateBack() {
        navigation.pop()
    }

    private fun createChild(
        config: AuthRootComponent.Config,
        componentContext: ComponentContext
    ): AuthRootComponent.Child =
        when (config) {
            is AuthRootComponent.Config.Login ->
                AuthRootComponent.Child.Login(
                    component =
                        DefaultLoginComponent(
                            componentContext = componentContext,
                            authController = authController,
                            onNavigateToApp = onAuthenticated,
                            onNavigateToRegister = ::navigateToRegister,
                            onNavigateToForgotPassword = ::navigateToForgotPassword
                        )
                )

            is AuthRootComponent.Config.Register ->
                AuthRootComponent.Child.Register(
                    component =
                        DefaultRegisterComponent(
                            componentContext = componentContext,
                            authController = authController,
                            onNavigateToApp = onAuthenticated,
                            onNavigateToLogin = ::navigateBack,
                            onBack = ::navigateBack
                        )
                )

            is AuthRootComponent.Config.ForgotPassword ->
                AuthRootComponent.Child.ForgotPassword(
                    component =
                        DefaultForgotPasswordComponent(
                            componentContext = componentContext,
                            authController = authController,
                            onNavigateToApp = onAuthenticated,
                            onBack = ::navigateBack
                        )
                )
        }
}
