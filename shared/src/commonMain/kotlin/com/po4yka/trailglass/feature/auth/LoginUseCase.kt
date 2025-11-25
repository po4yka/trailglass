package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.LoginResponse
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for user login.
 *
 * Authenticates user with email and password, stores tokens, and updates the user session.
 */
@Inject
class LoginUseCase(
    private val apiClient: TrailGlassApiClient,
    private val userSession: UserSession
) {
    private val logger = logger()

    /**
     * Execute login with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing LoginResponse on success, or Exception on failure
     */
    suspend fun execute(
        email: String,
        password: String
    ): Result<LoginResponse> {
        logger.info { "Attempting login for user: $email" }

        // Validate inputs
        if (email.isBlank()) {
            return Result.Error(
                TrailGlassError.ValidationError.RequiredFieldMissing("Email")
            )
        }
        if (password.isBlank()) {
            return Result.Error(
                TrailGlassError.ValidationError.RequiredFieldMissing("Password")
            )
        }
        if (!isValidEmail(email)) {
            return Result.Error(
                TrailGlassError.ValidationError.InvalidInput(
                    fieldName = "email",
                    technicalMessage = "Invalid email format"
                )
            )
        }

        return try {
            // Call API to login
            val result = apiClient.login(email, password)

            result
                .onSuccess { response ->
                    // Update user session with the authenticated user ID
                    userSession.setUserId(response.userId)
                    logger.info { "Login successful for user: ${response.email}" }
                }.onFailure { error ->
                    logger.error(error) { "Login failed for user: $email" }
                }

            // Convert kotlin.Result to domain Result
            result.fold(
                onSuccess = { response -> Result.Success(response) },
                onFailure = { exception ->
                    Result.Error(
                        TrailGlassError.NetworkError.RequestFailed(
                            technicalMessage = exception.message ?: "Login request failed",
                            cause = exception
                        )
                    )
                }
            )
        } catch (e: Exception) {
            logger.error(e) { "Login exception for user: $email" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Unknown login error",
                    cause = e
                )
            )
        }
    }

    /** Simple email validation. */
    private fun isValidEmail(email: String): Boolean = email.contains("@") && email.contains(".")
}
