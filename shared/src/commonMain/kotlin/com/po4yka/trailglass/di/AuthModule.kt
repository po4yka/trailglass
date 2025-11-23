package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.feature.auth.AuthController
import com.po4yka.trailglass.feature.auth.CheckAuthStatusUseCase
import com.po4yka.trailglass.feature.auth.LoginUseCase
import com.po4yka.trailglass.feature.auth.LogoutUseCase
import com.po4yka.trailglass.feature.auth.RegisterUseCase
import me.tatarka.inject.annotations.Provides

/**
 * Dependency injection module for authentication-related components.
 *
 * Provides:
 * - Authentication use cases (LoginUseCase, RegisterUseCase, LogoutUseCase, CheckAuthStatusUseCase)
 * - AuthController for managing authentication state
 * - UserSession for tracking the current user
 */
interface AuthModule {
    /** Provides UserSession for tracking authenticated user. */
    @Provides
    @AppScope
    fun provideUserSession(): UserSession {
        // For now, use a simple in-memory session
        // In production, this could be backed by secure storage
        return object : UserSession {
            private var currentUserId: String? = null

            override fun getCurrentUserId(): String? = currentUserId

            override fun isAuthenticated(): Boolean = currentUserId != null

            override fun setUserId(userId: String?) {
                currentUserId = userId
            }
        }
    }

    /** Provides LoginUseCase. Dependencies are automatically injected by kotlin-inject. */
    val loginUseCase: LoginUseCase

    /** Provides RegisterUseCase. Dependencies are automatically injected by kotlin-inject. */
    val registerUseCase: RegisterUseCase

    /** Provides LogoutUseCase. Dependencies are automatically injected by kotlin-inject. */
    val logoutUseCase: LogoutUseCase

    /** Provides CheckAuthStatusUseCase. Dependencies are automatically injected by kotlin-inject. */
    val checkAuthStatusUseCase: CheckAuthStatusUseCase

    /** Provides AuthController. Dependencies are automatically injected by kotlin-inject. */
    val authController: AuthController
}
