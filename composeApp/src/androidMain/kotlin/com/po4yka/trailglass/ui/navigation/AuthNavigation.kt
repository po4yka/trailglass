package com.po4yka.trailglass.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.po4yka.trailglass.feature.auth.AuthController
import com.po4yka.trailglass.ui.screens.auth.ForgotPasswordScreen
import com.po4yka.trailglass.ui.screens.auth.LoginScreen
import com.po4yka.trailglass.ui.screens.auth.RegisterScreen

/**
 * Main composable for authentication flow navigation.
 */
@Composable
fun AuthNavigation(
    authRootComponent: AuthRootComponent,
    modifier: Modifier = Modifier
) {
    val childStack by authRootComponent.childStack.subscribeAsState()
    val authController = (childStack.active.instance as? AuthRootComponent.Child.Login)?.component?.authController
        ?: (childStack.active.instance as? AuthRootComponent.Child.Register)?.component?.authController
        ?: (childStack.active.instance as? AuthRootComponent.Child.ForgotPassword)?.component?.authController

    // Watch for authentication success and trigger navigation
    val authState by authController!!.state.subscribeAsState()
    LaunchedEffect(authState) {
        if (authState is AuthController.AuthState.Authenticated) {
            authRootComponent.onAuthenticated()
        }
    }

    Children(
        stack = childStack,
        modifier = modifier
    ) { child ->
        when (val instance = child.instance) {
            is AuthRootComponent.Child.Login -> {
                LoginScreen(
                    authController = instance.component.authController,
                    onNavigateToRegister = instance.component.onNavigateToRegister,
                    onNavigateToForgotPassword = instance.component.onNavigateToForgotPassword
                )
            }

            is AuthRootComponent.Child.Register -> {
                RegisterScreen(
                    authController = instance.component.authController,
                    onNavigateToLogin = instance.component.onNavigateToLogin,
                    onNavigateBack = instance.component.onBack
                )
            }

            is AuthRootComponent.Child.ForgotPassword -> {
                ForgotPasswordScreen(
                    onNavigateBack = instance.component.onBack
                )
            }
        }
    }
}
