package com.po4yka.trailglass.data.remote.auth

import com.po4yka.trailglass.data.remote.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

/**
 * Token data class.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long // Timestamp in milliseconds
)

/**
 * Expect/actual for platform-specific secure token storage.
 */
expect class SecureTokenStorage {
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun getTokens(): AuthTokens?
    suspend fun clearTokens()
}

/**
 * Token provider implementation using secure storage.
 */
class TokenStorageProvider(
    private val secureStorage: SecureTokenStorage
) : TokenProvider {

    private val _authState = MutableStateFlow<AuthTokens?>(null)
    val authState: StateFlow<AuthTokens?> = _authState.asStateFlow()

    override suspend fun getAccessToken(): String? {
        val tokens = _authState.value ?: secureStorage.getTokens()
        _authState.value = tokens

        // Check if token is expired
        if (tokens != null && Clock.System.now().toEpochMilliseconds() >= tokens.expiresAt) {
            return null // Token expired
        }

        return tokens?.accessToken
    }

    override suspend fun getRefreshToken(): String? {
        val tokens = _authState.value ?: secureStorage.getTokens()
        _authState.value = tokens
        return tokens?.refreshToken
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        val expiresAt = Clock.System.now().toEpochMilliseconds() + (expiresIn * 1000)
        val tokens = AuthTokens(accessToken, refreshToken, expiresAt)
        secureStorage.saveTokens(tokens)
        _authState.value = tokens
    }

    override suspend fun clearTokens() {
        secureStorage.clearTokens()
        _authState.value = null
    }

    fun isAuthenticated(): Boolean {
        val tokens = _authState.value
        return tokens != null && Clock.System.now().toEpochMilliseconds() < tokens.expiresAt
    }
}
