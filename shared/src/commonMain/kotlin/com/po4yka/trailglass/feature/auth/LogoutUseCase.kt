package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for user logout.
 *
 * Logs out the user, clears tokens, and clears the user session.
 */
@Inject
class LogoutUseCase(
    private val apiClient: TrailGlassApiClient,
    private val userSession: UserSession
) {
    private val logger = logger()

    /**
     * Execute logout.
     *
     * Calls the backend to invalidate the session, then clears local tokens and session.
     *
     * @return Result<Unit> indicating success or failure
     */
    suspend fun execute(): Result<Unit> {
        logger.info { "Attempting logout for user: ${userSession.getCurrentUserId()}" }

        return try {
            // Call API to logout (best effort - ignore failures)
            apiClient.logout().onFailure { error ->
                logger.warn { "Logout API call failed (continuing anyway): ${error.message}" }
            }

            // Clear user session regardless of API call result
            userSession.setUserId(null)
            logger.info { "Logout successful, session cleared" }

            Result.Success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Logout exception" }
            // Still clear session even on exception
            userSession.setUserId(null)
            Result.Success(Unit)
        }
    }
}
