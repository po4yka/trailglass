package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.remote.DeviceInfoProvider
import com.po4yka.trailglass.data.remote.TokenProvider
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.*

/**
 * Mock implementation of TrailGlassApiClient for testing.
 */
open class MockTrailGlassApiClient : TrailGlassApiClient(
    config = ApiConfig(baseUrl = "http://localhost:8080/api/v1", enableLogging = false),
    tokenProvider = MockTokenProvider(),
    deviceInfoProvider = MockDeviceInfoProvider()
) {
    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<RegisterResponse> {
        return Result.failure(NotImplementedError("Override this method in tests"))
    }

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return Result.failure(NotImplementedError("Override this method in tests"))
    }

    override suspend fun logout(): Result<Unit> {
        return Result.failure(NotImplementedError("Override this method in tests"))
    }
}

/**
 * Mock implementation of UserSession for testing.
 */
class MockUserSession : UserSession {
    private var currentUserId: String? = null

    override fun getCurrentUserId(): String? = currentUserId

    override fun isAuthenticated(): Boolean = currentUserId != null

    override fun setUserId(userId: String?) {
        currentUserId = userId
    }
}

/**
 * Mock implementation of TokenProvider for testing.
 */
class MockTokenProvider : TokenProvider {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}

/**
 * Mock implementation of DeviceInfoProvider for testing.
 */
class MockDeviceInfoProvider : DeviceInfoProvider {
    override fun getDeviceId(): String = "test-device-id"

    override fun getDeviceName(): String = "Test Device"

    override fun getPlatform(): String = "Test Platform"

    override fun getOsVersion(): String = "1.0.0"

    override fun getAppVersion(): String = "1.0.0"
}
