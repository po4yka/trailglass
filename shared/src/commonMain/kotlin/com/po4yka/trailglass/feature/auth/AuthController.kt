package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for authentication feature. Manages authentication state, login, registration, and logout flows.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class AuthController(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val userSession: UserSession,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** Authentication state. */
    sealed class AuthState {
        /** Initial state - checking if user is already authenticated. */
        object Initializing : AuthState()

        /** User is not authenticated. */
        object Unauthenticated : AuthState()

        /**
         * User is using the app in guest mode (no account required). All data is stored locally only, no sync
         * functionality.
         */
        object Guest : AuthState()

        /** User is authenticated with an account. */
        data class Authenticated(
            val userId: String,
            val email: String,
            val displayName: String
        ) : AuthState()

        /** Authentication in progress (login or register). */
        data class Loading(
            val operation: String
        ) : AuthState()

        /** Authentication error. */
        data class Error(
            val message: String,
            val previousState: AuthState
        ) : AuthState()
    }

    private val _state = MutableStateFlow<AuthState>(AuthState.Initializing)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Check if user is already authenticated on initialization
        checkAuthStatus()
    }

    /** Check if user is already authenticated. */
    fun checkAuthStatus() {
        logger.debug { "Checking authentication status" }

        controllerScope.launch {
            try {
                // Check if user is in guest mode
                if (userSession.isGuest()) {
                    logger.debug { "User is in guest mode" }
                    _state.value = AuthState.Guest
                    return@launch
                }

                val isAuthenticated = checkAuthStatusUseCase.execute()
                // User is either authenticated or not - both cases set Unauthenticated for now
                // In a real app, we'd fetch user profile here if authenticated
                _state.value = AuthState.Unauthenticated
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Failed to check auth status" }
                _state.value = AuthState.Unauthenticated
            }
        }
    }

    /** Login with email and password. */
    fun login(
        email: String,
        password: String
    ) {
        logger.info { "Login requested for: $email" }

        // Atomically capture previous state and set loading state
        val previousState = _state.getAndUpdate { AuthState.Loading("Logging in...") }

        controllerScope.launch {
            try {
                when (val result = loginUseCase.execute(email, password)) {
                    is Result.Success -> {
                        val response = result.data
                        _state.value =
                            AuthState.Authenticated(
                                userId = response.userId,
                                email = response.email,
                                displayName = response.displayName
                            )
                        logger.info { "Login successful for: ${response.email}" }
                    }
                    is Result.Error -> {
                        val error = result.error
                        val errorMessage = error.getUserFriendlyMessage()
                        _state.value = AuthState.Error(errorMessage, previousState)
                        logger.error { "Login failed for: $email - ${error.getTechnicalDetails()}" }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value =
                    AuthState.Error(
                        e.message ?: "An unexpected error occurred",
                        previousState
                    )
                logger.error(e) { "Login exception for: $email" }
            }
        }
    }

    /** Register a new user. */
    fun register(
        email: String,
        password: String,
        displayName: String
    ) {
        logger.info { "Registration requested for: $email" }

        // Atomically capture previous state and set loading state
        val previousState = _state.getAndUpdate { AuthState.Loading("Creating account...") }

        controllerScope.launch {
            try {
                when (val result = registerUseCase.execute(email, password, displayName)) {
                    is Result.Success -> {
                        val response = result.data
                        _state.value =
                            AuthState.Authenticated(
                                userId = response.userId,
                                email = response.email,
                                displayName = response.displayName
                            )
                        logger.info { "Registration successful for: ${response.email}" }
                    }
                    is Result.Error -> {
                        val error = result.error
                        val errorMessage = error.getUserFriendlyMessage()
                        _state.value = AuthState.Error(errorMessage, previousState)
                        logger.error { "Registration failed for: $email - ${error.getTechnicalDetails()}" }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value =
                    AuthState.Error(
                        e.message ?: "An unexpected error occurred",
                        previousState
                    )
                logger.error(e) { "Registration exception for: $email" }
            }
        }
    }

    /** Logout the current user. */
    fun logout() {
        logger.info { "Logout requested" }

        controllerScope.launch {
            try {
                logoutUseCase.execute()
                _state.value = AuthState.Unauthenticated
                logger.info { "Logout successful" }
            } catch (e: Exception) {
                // Even if logout fails, we should clear the local state
                _state.value = AuthState.Unauthenticated
                logger.error(e) { "Logout exception (session cleared anyway)" }
            }
        }
    }

    /**
     * Continue using the app as a guest (without creating an account). All data will be stored locally only, with no
     * cloud sync.
     */
    fun continueAsGuest() {
        logger.info { "User continuing as guest" }

        controllerScope.launch {
            try {
                // Set a special guest user ID in the session
                // This allows the app to function normally but skip sync operations
                userSession.setUserId(UserSession.GUEST_USER_ID)
                _state.value = AuthState.Guest
                logger.info { "Guest mode activated" }
            } catch (e: Exception) {
                _state.value =
                    AuthState.Error(
                        "Failed to enter guest mode: ${e.message}",
                        AuthState.Unauthenticated
                    )
                logger.error(e) { "Failed to activate guest mode" }
            }
        }
    }

    /** Check if user is currently in guest mode. */
    fun isGuest(): Boolean =
        _state.value is AuthState.Guest ||
            userSession.getCurrentUserId() == UserSession.GUEST_USER_ID

    /** Clear error state and return to previous state. */
    fun clearError() {
        val currentState = _state.value
        if (currentState is AuthState.Error) {
            _state.value = currentState.previousState
        }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks. MUST be called when this controller is no longer
     * needed.
     */
    override fun cleanup() {
        logger.info { "Cleaning up AuthController" }
        controllerScope.cancel()
        logger.debug { "AuthController cleanup complete" }
    }
}
