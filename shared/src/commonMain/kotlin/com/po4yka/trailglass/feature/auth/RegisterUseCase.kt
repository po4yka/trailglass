package com.po4yka.trailglass.feature.auth

import com.po4yka.trailglass.data.auth.UserSession
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.RegisterResponse
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for user registration.
 *
 * Registers a new user account, stores tokens, and updates the user session.
 */
@Inject
class RegisterUseCase(
    private val apiClient: TrailGlassApiClient,
    private val userSession: UserSession
) {
    private val logger = logger()

    /**
     * Execute registration with email, password, and display name.
     *
     * @param email User's email address
     * @param password User's password
     * @param displayName User's display name
     * @return Result containing RegisterResponse on success, or Exception on failure
     */
    suspend fun execute(
        email: String,
        password: String,
        displayName: String
    ): Result<RegisterResponse> {
        logger.info { "Attempting registration for email: $email" }

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
        if (displayName.isBlank()) {
            return Result.Error(
                TrailGlassError.ValidationError.RequiredFieldMissing("Display name")
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
        if (password.length < 8) {
            return Result.Error(
                TrailGlassError.ValidationError.InvalidInput(
                    fieldName = "password",
                    technicalMessage = "Password must be at least 8 characters"
                )
            )
        }

        return try {
            // Call API to register
            val result = apiClient.register(email, password, displayName)

            result
                .onSuccess { response ->
                    // Update user session with the new user ID
                    userSession.setUserId(response.userId)
                    logger.info { "Registration successful for user: ${response.email}" }
                }.onFailure { error ->
                    logger.error(error) { "Registration failed for email: $email" }
                }

            // Convert kotlin.Result to domain Result
            result.fold(
                onSuccess = { response -> Result.Success(response) },
                onFailure = { exception ->
                    Result.Error(
                        TrailGlassError.NetworkError.RequestFailed(
                            technicalMessage = exception.message ?: "Registration request failed",
                            cause = exception
                        )
                    )
                }
            )
        } catch (e: Exception) {
            logger.error(e) { "Registration exception for email: $email" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Unknown registration error",
                    cause = e
                )
            )
        }
    }

    /** Simple email validation. */
    private fun isValidEmail(email: String): Boolean = email.contains("@") && email.contains(".")
}
