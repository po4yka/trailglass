package com.po4yka.trailglass.domain.error

import kotlinx.datetime.Instant

/**
 * Domain error hierarchy for TrailGlass.
 *
 * Provides user-friendly error messages and structured error handling.
 */
sealed class TrailGlassError(
    open val userMessage: String,
    open val technicalMessage: String? = null,
    open val cause: Throwable? = null,
    open val timestamp: Instant =
        kotlinx.datetime.Clock.System
            .now(),
    open val errorCode: String? = null
) {
    /**
     * Network-related errors.
     */
    sealed class NetworkError(
        userMessage: String,
        technicalMessage: String? = null,
        cause: Throwable? = null,
        errorCode: String? = null
    ) : TrailGlassError(userMessage, technicalMessage, cause, errorCode = errorCode) {
        data class NoConnection(
            override val cause: Throwable? = null
        ) : NetworkError(
                userMessage = "No internet connection. Please check your network settings.",
                technicalMessage = "Network connection unavailable",
                cause = cause,
                errorCode = "NET_001"
            )

        data class Timeout(
            override val cause: Throwable? = null
        ) : NetworkError(
                userMessage = "Request timed out. Please try again.",
                technicalMessage = "Network request exceeded timeout",
                cause = cause,
                errorCode = "NET_002"
            )

        data class ServerError(
            val statusCode: Int,
            override val cause: Throwable? = null
        ) : NetworkError(
                userMessage = "Server error occurred. Please try again later.",
                technicalMessage = "Server returned status code: $statusCode",
                cause = cause,
                errorCode = "NET_003"
            )

        data class RequestFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : NetworkError(
                userMessage = "Request failed. Please check your connection and try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "NET_004"
            )
    }

    /**
     * Database-related errors.
     */
    sealed class DatabaseError(
        userMessage: String,
        technicalMessage: String? = null,
        cause: Throwable? = null,
        errorCode: String? = null
    ) : TrailGlassError(userMessage, technicalMessage, cause, errorCode = errorCode) {
        data class QueryFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : DatabaseError(
                userMessage = "Failed to load data. Please try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "DB_001"
            )

        data class InsertFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : DatabaseError(
                userMessage = "Failed to save data. Please try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "DB_002"
            )

        data class UpdateFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : DatabaseError(
                userMessage = "Failed to update data. Please try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "DB_003"
            )

        data class DeleteFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : DatabaseError(
                userMessage = "Failed to delete data. Please try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "DB_004"
            )

        data class ConstraintViolation(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : DatabaseError(
                userMessage = "Operation failed due to data constraint. Please check your input.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "DB_005"
            )
    }

    /**
     * Location-related errors.
     */
    sealed class LocationError(
        userMessage: String,
        technicalMessage: String? = null,
        cause: Throwable? = null,
        errorCode: String? = null
    ) : TrailGlassError(userMessage, technicalMessage, cause, errorCode = errorCode) {
        data class PermissionDenied(
            override val cause: Throwable? = null
        ) : LocationError(
                userMessage = "Location permission required. Please grant location access in settings.",
                technicalMessage = "Location permission not granted",
                cause = cause,
                errorCode = "LOC_001"
            )

        data class LocationUnavailable(
            override val cause: Throwable? = null
        ) : LocationError(
                userMessage = "Location unavailable. Please ensure location services are enabled.",
                technicalMessage = "Location services unavailable",
                cause = cause,
                errorCode = "LOC_002"
            )

        data class TrackingFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : LocationError(
                userMessage = "Location tracking failed. Please try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "LOC_003"
            )

        data class GeocodingFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : LocationError(
                userMessage = "Failed to determine location address. This won't affect your trip tracking.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "LOC_004"
            )
    }

    /**
     * Photo-related errors.
     */
    sealed class PhotoError(
        userMessage: String,
        technicalMessage: String? = null,
        cause: Throwable? = null,
        errorCode: String? = null
    ) : TrailGlassError(userMessage, technicalMessage, cause, errorCode = errorCode) {
        data class PermissionDenied(
            override val cause: Throwable? = null
        ) : PhotoError(
                userMessage = "Photo library access required. Please grant permission in settings.",
                technicalMessage = "Photo library permission not granted",
                cause = cause,
                errorCode = "PHOTO_001"
            )

        data class LoadFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : PhotoError(
                userMessage = "Failed to load photo. Please try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "PHOTO_002"
            )

        data class AttachmentFailed(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : PhotoError(
                userMessage = "Failed to attach photo. Please try again.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "PHOTO_003"
            )

        data class InvalidPhoto(
            override val technicalMessage: String,
            override val cause: Throwable? = null
        ) : PhotoError(
                userMessage = "Invalid or corrupted photo file.",
                technicalMessage = technicalMessage,
                cause = cause,
                errorCode = "PHOTO_004"
            )
    }

    /**
     * Validation errors.
     */
    sealed class ValidationError(
        userMessage: String,
        technicalMessage: String? = null,
        cause: Throwable? = null,
        errorCode: String? = null
    ) : TrailGlassError(userMessage, technicalMessage, cause, errorCode = errorCode) {
        data class InvalidInput(
            val fieldName: String,
            override val technicalMessage: String
        ) : ValidationError(
                userMessage = "Invalid $fieldName. Please check your input.",
                technicalMessage = technicalMessage,
                errorCode = "VAL_001"
            )

        data class RequiredFieldMissing(
            val fieldName: String
        ) : ValidationError(
                userMessage = "$fieldName is required.",
                technicalMessage = "Required field missing: $fieldName",
                errorCode = "VAL_002"
            )

        data class InvalidCoordinate(
            override val technicalMessage: String
        ) : ValidationError(
                userMessage = "Invalid location coordinates.",
                technicalMessage = technicalMessage,
                errorCode = "VAL_003"
            )

        data class InvalidDateRange(
            override val technicalMessage: String
        ) : ValidationError(
                userMessage = "Invalid date range. End date must be after start date.",
                technicalMessage = technicalMessage,
                errorCode = "VAL_004"
            )
    }

    /**
     * Unknown or unexpected errors.
     */
    data class Unknown(
        override val technicalMessage: String,
        override val cause: Throwable? = null
    ) : TrailGlassError(
            userMessage = "An unexpected error occurred. Please try again.",
            technicalMessage = technicalMessage,
            cause = cause,
            errorCode = "UNK_001"
        )

    /**
     * Get a user-friendly error message for display.
     */
    fun getUserFriendlyMessage(): String = userMessage

    /**
     * Get technical details for logging/debugging.
     */
    fun getTechnicalDetails(): String =
        buildString {
            append("Error Code: ${errorCode ?: "NONE"}")
            append(" | Message: ${technicalMessage ?: userMessage}")
            cause?.let {
                append(" | Cause: ${it.message}")
            }
        }

    /**
     * Check if this error is recoverable with a retry.
     */
    fun isRetryable(): Boolean =
        when (this) {
            is NetworkError.Timeout,
            is NetworkError.RequestFailed,
            is NetworkError.ServerError,
            is DatabaseError.QueryFailed -> true
            else -> false
        }

    /**
     * Check if this error requires user action (e.g., permissions).
     */
    fun requiresUserAction(): Boolean =
        when (this) {
            is LocationError.PermissionDenied,
            is PhotoError.PermissionDenied,
            is NetworkError.NoConnection -> true
            else -> false
        }
}

/**
 * Result wrapper for operations that can fail.
 */
sealed class Result<out T> {
    data class Success<T>(
        val data: T
    ) : Result<T>()

    data class Error(
        val error: TrailGlassError
    ) : Result<Nothing>()

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error

    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Error -> null
        }

    fun getErrorOrNull(): TrailGlassError? =
        when (this) {
            is Success -> null
            is Error -> error
        }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (TrailGlassError) -> Unit): Result<T> {
        if (this is Error) action(error)
        return this
    }

    inline fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(error)
        }

    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> =
        when (this) {
            is Success -> transform(data)
            is Error -> Error(error)
        }
}

/**
 * Execute a block and wrap result/exception in Result.
 */
inline fun <T> resultOf(block: () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(
            TrailGlassError.Unknown(
                technicalMessage = e.message ?: "Unknown error",
                cause = e
            )
        )
    }
