package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.remote.TokenProvider
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case to check if user is authenticated.
 *
 * Checks both user session and token validity.
 */
@Inject
class CheckAuthStatusUseCase(
    private val userSession: UserSession,
    private val tokenProvider: TokenProvider
) {
    private val logger = logger()

    /**
     * Check if user is currently authenticated.
     *
     * @return true if user has valid authentication, false otherwise
     */
    suspend fun execute(): Boolean {
        logger.debug { "Checking authentication status" }

        // Check if user session exists
        val hasSession = userSession.isAuthenticated()
        if (!hasSession) {
            logger.debug { "No user session found" }
            return false
        }

        // Check if we have a valid access token
        val accessToken = tokenProvider.getAccessToken()
        if (accessToken == null) {
            logger.debug { "No valid access token found" }
            // Clear session if token is missing
            userSession.setUserId(null)
            return false
        }

        logger.debug { "User is authenticated: ${userSession.getCurrentUserId()}" }
        return true
    }
}
