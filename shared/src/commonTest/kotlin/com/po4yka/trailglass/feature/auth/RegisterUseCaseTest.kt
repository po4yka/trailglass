package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.RegisterResponse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Unit tests for RegisterUseCase.
 */
class RegisterUseCaseTest {

    @Test
    fun `execute with valid inputs should succeed and update session`() = runTest {
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
        val useCase = RegisterUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("test@example.com", "password123", "Test User")

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe mockResponse
        mockUserSession.getCurrentUserId() shouldBe "user123"
    }

    @Test
    fun `execute with empty email should fail`() = runTest {
        // Arrange
        val mockApiClient = MockTrailGlassApiClient()
        val mockUserSession = MockUserSession()
        val useCase = RegisterUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("", "password123", "Test User")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.shouldBeInstanceOf<IllegalArgumentException>()
        result.exceptionOrNull()?.message shouldBe "Email cannot be empty"
    }

    @Test
    fun `execute with short password should fail`() = runTest {
        // Arrange
        val mockApiClient = MockTrailGlassApiClient()
        val mockUserSession = MockUserSession()
        val useCase = RegisterUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("test@example.com", "short", "Test User")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.shouldBeInstanceOf<IllegalArgumentException>()
        result.exceptionOrNull()?.message shouldBe "Password must be at least 8 characters"
    }

    @Test
    fun `execute with empty display name should fail`() = runTest {
        // Arrange
        val mockApiClient = MockTrailGlassApiClient()
        val mockUserSession = MockUserSession()
        val useCase = RegisterUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("test@example.com", "password123", "")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.shouldBeInstanceOf<IllegalArgumentException>()
        result.exceptionOrNull()?.message shouldBe "Display name cannot be empty"
    }

    @Test
    fun `execute with API failure should fail and not update session`() = runTest {
        // Arrange
        val mockApiClient = object : MockTrailGlassApiClient() {
            override suspend fun register(
                email: String,
                password: String,
                displayName: String
            ): Result<RegisterResponse> {
                return Result.failure(Exception("Email already exists"))
            }
        }

        val mockUserSession = MockUserSession()
        val useCase = RegisterUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("test@example.com", "password123", "Test User")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.message shouldBe "Email already exists"
        mockUserSession.getCurrentUserId() shouldBe null
    }
}
