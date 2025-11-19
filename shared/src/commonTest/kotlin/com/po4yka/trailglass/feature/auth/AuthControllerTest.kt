package com.po4yka.trailglass.feature.auth

import app.cash.turbine.test
import com.po4yka.trailglass.data.remote.dto.LoginResponse
import com.po4yka.trailglass.data.remote.dto.RegisterResponse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * Unit tests for AuthController.
 */
class AuthControllerTest {

    private lateinit var controller: AuthController

    @AfterTest
    fun cleanup() {
        if (::controller.isInitialized) {
            controller.cleanup()
        }
    }

    @Test
    fun `initial state should be Initializing then Unauthenticated`() = runTest {
        // Arrange
        val mockApiClient = MockTrailGlassApiClient()
        val mockUserSession = MockUserSession()
        val mockTokenProvider = MockTokenProvider()

        val loginUseCase = LoginUseCase(mockApiClient, mockUserSession)
        val registerUseCase = RegisterUseCase(mockApiClient, mockUserSession)
        val logoutUseCase = LogoutUseCase(mockApiClient, mockUserSession)
        val checkAuthUseCase = CheckAuthStatusUseCase(mockUserSession, mockTokenProvider)

        // Act
        controller = AuthController(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            logoutUseCase = logoutUseCase,
            checkAuthStatusUseCase = checkAuthUseCase,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )

        // Assert
        controller.state.test {
            val initialState = awaitItem()
            initialState.shouldBeInstanceOf<AuthController.AuthState.Initializing>()

            val nextState = awaitItem()
            nextState.shouldBeInstanceOf<AuthController.AuthState.Unauthenticated>()
        }
    }

    @Test
    fun `login with valid credentials should transition to Authenticated`() = runTest {
        // Arrange
        val mockResponse = LoginResponse(
            userId = "user123",
            email = "test@example.com",
            displayName = "Test User",
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresIn = 3600,
            lastSyncTimestamp = null
        )

        val mockApiClient = object : MockTrailGlassApiClient() {
            override suspend fun login(email: String, password: String): Result<LoginResponse> {
                return Result.success(mockResponse)
            }
        }

        val mockUserSession = MockUserSession()
        val mockTokenProvider = MockTokenProvider()

        val loginUseCase = LoginUseCase(mockApiClient, mockUserSession)
        val registerUseCase = RegisterUseCase(mockApiClient, mockUserSession)
        val logoutUseCase = LogoutUseCase(mockApiClient, mockUserSession)
        val checkAuthUseCase = CheckAuthStatusUseCase(mockUserSession, mockTokenProvider)

        controller = AuthController(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            logoutUseCase = logoutUseCase,
            checkAuthStatusUseCase = checkAuthUseCase,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )

        controller.state.test {
            // Skip initial states
            awaitItem() // Initializing
            awaitItem() // Unauthenticated

            // Act
            controller.login("test@example.com", "password123")

            // Assert
            val loadingState = awaitItem()
            loadingState.shouldBeInstanceOf<AuthController.AuthState.Loading>()

            val authenticatedState = awaitItem()
            authenticatedState.shouldBeInstanceOf<AuthController.AuthState.Authenticated>()
            (authenticatedState as AuthController.AuthState.Authenticated).userId shouldBe "user123"
            authenticatedState.email shouldBe "test@example.com"
            authenticatedState.displayName shouldBe "Test User"
        }
    }

    @Test
    fun `login with invalid credentials should transition to Error`() = runTest {
        // Arrange
        val mockApiClient = object : MockTrailGlassApiClient() {
            override suspend fun login(email: String, password: String): Result<LoginResponse> {
                return Result.failure(Exception("401 Unauthorized"))
            }
        }

        val mockUserSession = MockUserSession()
        val mockTokenProvider = MockTokenProvider()

        val loginUseCase = LoginUseCase(mockApiClient, mockUserSession)
        val registerUseCase = RegisterUseCase(mockApiClient, mockUserSession)
        val logoutUseCase = LogoutUseCase(mockApiClient, mockUserSession)
        val checkAuthUseCase = CheckAuthStatusUseCase(mockUserSession, mockTokenProvider)

        controller = AuthController(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            logoutUseCase = logoutUseCase,
            checkAuthStatusUseCase = checkAuthUseCase,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )

        controller.state.test {
            // Skip initial states
            awaitItem() // Initializing
            awaitItem() // Unauthenticated

            // Act
            controller.login("test@example.com", "wrongpassword")

            // Assert
            val loadingState = awaitItem()
            loadingState.shouldBeInstanceOf<AuthController.AuthState.Loading>()

            val errorState = awaitItem()
            errorState.shouldBeInstanceOf<AuthController.AuthState.Error>()
            (errorState as AuthController.AuthState.Error).message shouldBe "Invalid email or password"
        }
    }

    @Test
    fun `register with valid inputs should transition to Authenticated`() = runTest {
        // Arrange
        val mockResponse = RegisterResponse(
            userId = "user123",
            email = "test@example.com",
            displayName = "Test User",
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresIn = 3600,
            createdAt = "2025-11-18T12:00:00Z"
        )

        val mockApiClient = object : MockTrailGlassApiClient() {
            override suspend fun register(
                email: String,
                password: String,
                displayName: String
            ): Result<RegisterResponse> {
                return Result.success(mockResponse)
            }
        }

        val mockUserSession = MockUserSession()
        val mockTokenProvider = MockTokenProvider()

        val loginUseCase = LoginUseCase(mockApiClient, mockUserSession)
        val registerUseCase = RegisterUseCase(mockApiClient, mockUserSession)
        val logoutUseCase = LogoutUseCase(mockApiClient, mockUserSession)
        val checkAuthUseCase = CheckAuthStatusUseCase(mockUserSession, mockTokenProvider)

        controller = AuthController(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            logoutUseCase = logoutUseCase,
            checkAuthStatusUseCase = checkAuthUseCase,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )

        controller.state.test {
            // Skip initial states
            awaitItem() // Initializing
            awaitItem() // Unauthenticated

            // Act
            controller.register("test@example.com", "password123", "Test User")

            // Assert
            val loadingState = awaitItem()
            loadingState.shouldBeInstanceOf<AuthController.AuthState.Loading>()

            val authenticatedState = awaitItem()
            authenticatedState.shouldBeInstanceOf<AuthController.AuthState.Authenticated>()
            (authenticatedState as AuthController.AuthState.Authenticated).userId shouldBe "user123"
        }
    }

    @Test
    fun `logout should transition to Unauthenticated`() = runTest {
        // Arrange
        val mockApiClient = object : MockTrailGlassApiClient() {
            override suspend fun logout(): Result<Unit> {
                return Result.success(Unit)
            }
        }

        val mockUserSession = MockUserSession()
        mockUserSession.setUserId("user123") // Set initial authenticated state

        val mockTokenProvider = MockTokenProvider()

        val loginUseCase = LoginUseCase(mockApiClient, mockUserSession)
        val registerUseCase = RegisterUseCase(mockApiClient, mockUserSession)
        val logoutUseCase = LogoutUseCase(mockApiClient, mockUserSession)
        val checkAuthUseCase = CheckAuthStatusUseCase(mockUserSession, mockTokenProvider)

        controller = AuthController(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            logoutUseCase = logoutUseCase,
            checkAuthStatusUseCase = checkAuthUseCase,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )

        controller.state.test {
            // Skip initial states
            awaitItem() // Initializing
            awaitItem() // Unauthenticated (checkAuth didn't find token)

            // Act
            controller.logout()

            // Assert
            val unauthenticatedState = awaitItem()
            unauthenticatedState.shouldBeInstanceOf<AuthController.AuthState.Unauthenticated>()
            mockUserSession.isAuthenticated() shouldBe false
        }
    }
}
