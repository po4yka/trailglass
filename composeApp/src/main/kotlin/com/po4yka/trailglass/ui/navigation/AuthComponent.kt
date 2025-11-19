package com.po4yka.trailglass.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.po4yka.trailglass.feature.auth.AuthController

/**
 * Component for authentication screens (Login, Register, ForgotPassword).
 */
interface AuthComponent {
    val authController: AuthController
    val onNavigateToApp: () -> Unit
}

/**
 * Component for Login screen.
 */
interface LoginComponent : AuthComponent {
    val onNavigateToRegister: () -> Unit
    val onNavigateToForgotPassword: () -> Unit
}

/**
 * Default implementation of LoginComponent.
 */
class DefaultLoginComponent(
    componentContext: ComponentContext,
    override val authController: AuthController,
    override val onNavigateToApp: () -> Unit,
    override val onNavigateToRegister: () -> Unit,
    override val onNavigateToForgotPassword: () -> Unit
) : LoginComponent, ComponentContext by componentContext

/**
 * Component for Register screen.
 */
interface RegisterComponent : AuthComponent {
    val onNavigateToLogin: () -> Unit
    val onBack: () -> Unit
}

/**
 * Default implementation of RegisterComponent.
 */
class DefaultRegisterComponent(
    componentContext: ComponentContext,
    override val authController: AuthController,
    override val onNavigateToApp: () -> Unit,
    override val onNavigateToLogin: () -> Unit,
    override val onBack: () -> Unit
) : RegisterComponent, ComponentContext by componentContext

/**
 * Component for ForgotPassword screen.
 */
interface ForgotPasswordComponent : AuthComponent {
    val onBack: () -> Unit
}

/**
 * Default implementation of ForgotPasswordComponent.
 */
class DefaultForgotPasswordComponent(
    componentContext: ComponentContext,
    override val authController: AuthController,
    override val onNavigateToApp: () -> Unit,
    override val onBack: () -> Unit
) : ForgotPasswordComponent, ComponentContext by componentContext
