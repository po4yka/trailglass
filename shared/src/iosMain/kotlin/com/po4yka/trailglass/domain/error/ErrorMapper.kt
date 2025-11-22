package com.po4yka.trailglass.domain.error

import kotlinx.coroutines.TimeoutCancellationException

/**
 * iOS implementation of ErrorMapper.
 * Maps iOS/Darwin-specific exceptions to TrailGlassError instances.
 */
actual object ErrorMapper {
    /**
     * Map a generic exception to TrailGlassError.
     */
    actual fun mapException(exception: Throwable): TrailGlassError =
        when (exception) {
            // Network errors
            is TimeoutCancellationException -> TrailGlassError.NetworkError.Timeout(exception)

            // Validation errors
            is IllegalArgumentException ->
                TrailGlassError.ValidationError.InvalidInput(
                    fieldName = "input",
                    technicalMessage = exception.message ?: "Invalid argument"
                )
            is IllegalStateException ->
                TrailGlassError.ValidationError.InvalidInput(
                    fieldName = "state",
                    technicalMessage = exception.message ?: "Invalid state"
                )

            // Check for NSError-based exceptions
            else -> {
                // Try to extract NSError information if available
                val message = exception.message ?: exception::class.simpleName ?: "Unknown error"

                // Map based on error message patterns
                when {
                    message.contains("network", ignoreCase = true) ||
                        message.contains("connection", ignoreCase = true) -> {
                        TrailGlassError.NetworkError.RequestFailed(
                            technicalMessage = message,
                            cause = exception
                        )
                    }
                    message.contains("timeout", ignoreCase = true) -> {
                        TrailGlassError.NetworkError.Timeout(exception)
                    }
                    message.contains("database", ignoreCase = true) ||
                        message.contains("sql", ignoreCase = true) -> {
                        TrailGlassError.DatabaseError.QueryFailed(
                            technicalMessage = message,
                            cause = exception
                        )
                    }
                    message.contains("permission", ignoreCase = true) ||
                        message.contains("denied", ignoreCase = true) -> {
                        TrailGlassError.LocationError.PermissionDenied(exception)
                    }
                    else ->
                        TrailGlassError.Unknown(
                            technicalMessage = message,
                            cause = exception
                        )
                }
            }
        }

    /**
     * Map a database exception to TrailGlassError.
     */
    actual fun mapDatabaseException(
        exception: Throwable,
        operation: DatabaseOperation
    ): TrailGlassError {
        val technicalMessage = exception.message ?: "Database operation failed"

        return when (operation) {
            DatabaseOperation.QUERY ->
                TrailGlassError.DatabaseError.QueryFailed(
                    technicalMessage = technicalMessage,
                    cause = exception
                )
            DatabaseOperation.INSERT ->
                TrailGlassError.DatabaseError.InsertFailed(
                    technicalMessage = technicalMessage,
                    cause = exception
                )
            DatabaseOperation.UPDATE ->
                TrailGlassError.DatabaseError.UpdateFailed(
                    technicalMessage = technicalMessage,
                    cause = exception
                )
            DatabaseOperation.DELETE ->
                TrailGlassError.DatabaseError.DeleteFailed(
                    technicalMessage = technicalMessage,
                    cause = exception
                )
        }
    }

    /**
     * Map a location exception to TrailGlassError.
     */
    actual fun mapLocationException(
        exception: Throwable,
        context: LocationContext
    ): TrailGlassError =
        when (context) {
            LocationContext.PERMISSION -> TrailGlassError.LocationError.PermissionDenied(exception)
            LocationContext.UNAVAILABLE -> TrailGlassError.LocationError.LocationUnavailable(exception)
            LocationContext.TRACKING ->
                TrailGlassError.LocationError.TrackingFailed(
                    technicalMessage = exception.message ?: "Tracking failed",
                    cause = exception
                )
            LocationContext.GEOCODING ->
                TrailGlassError.LocationError.GeocodingFailed(
                    technicalMessage = exception.message ?: "Geocoding failed",
                    cause = exception
                )
        }

    /**
     * Map a photo exception to TrailGlassError.
     */
    actual fun mapPhotoException(
        exception: Throwable,
        context: PhotoContext
    ): TrailGlassError =
        when (context) {
            PhotoContext.PERMISSION -> TrailGlassError.PhotoError.PermissionDenied(exception)
            PhotoContext.LOAD ->
                TrailGlassError.PhotoError.LoadFailed(
                    technicalMessage = exception.message ?: "Load failed",
                    cause = exception
                )
            PhotoContext.ATTACH ->
                TrailGlassError.PhotoError.AttachmentFailed(
                    technicalMessage = exception.message ?: "Attachment failed",
                    cause = exception
                )
            PhotoContext.INVALID ->
                TrailGlassError.PhotoError.InvalidPhoto(
                    technicalMessage = exception.message ?: "Invalid photo",
                    cause = exception
                )
        }

    /**
     * Execute a block and map any exception to TrailGlassError.
     */
    actual inline fun <T> mapToResult(block: () -> T): Result<T> =
        try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }

    /**
     * Execute a suspending block and map any exception to TrailGlassError.
     */
    actual suspend inline fun <T> mapToResultSuspend(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
}
