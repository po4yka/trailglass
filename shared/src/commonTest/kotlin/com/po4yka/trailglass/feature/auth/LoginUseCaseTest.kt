package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.LoginResponse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Unit tests for LoginUseCase.
 */
class LoginUseCaseTest {

    @Test
    fun `execute with valid credentials should succeed and update session`() = runTest {
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
        val useCase = LoginUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("test@example.com", "password123")

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
        val useCase = LoginUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("", "password123")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.shouldBeInstanceOf<IllegalArgumentException>()
        result.exceptionOrNull()?.message shouldBe "Email cannot be empty"
    }

    @Test
    fun `execute with empty password should fail`() = runTest {
        // Arrange
        val mockApiClient = MockTrailGlassApiClient()
        val mockUserSession = MockUserSession()
        val useCase = LoginUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("test@example.com", "")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.shouldBeInstanceOf<IllegalArgumentException>()
        result.exceptionOrNull()?.message shouldBe "Password cannot be empty"
    }

    @Test
    fun `execute with invalid email format should fail`() = runTest {
        // Arrange
        val mockApiClient = MockTrailGlassApiClient()
        val mockUserSession = MockUserSession()
        val useCase = LoginUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("notanemail", "password123")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.shouldBeInstanceOf<IllegalArgumentException>()
        result.exceptionOrNull()?.message shouldBe "Invalid email format"
    }

    @Test
    fun `execute with API failure should fail and not update session`() = runTest {
        // Arrange
        val mockApiClient = object : MockTrailGlassApiClient() {
            override suspend fun login(email: String, password: String): Result<LoginResponse> {
                return Result.failure(Exception("Unauthorized"))
            }
        }

        val mockUserSession = MockUserSession()
        val useCase = LoginUseCase(mockApiClient, mockUserSession)

        // Act
        val result = useCase.execute("test@example.com", "wrongpassword")

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()?.message shouldBe "Unauthorized"
        mockUserSession.getCurrentUserId() shouldBe null
    }
}
