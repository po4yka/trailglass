package com.po4yka.trailglass.domain.error

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Maps exceptions to user-friendly TrailGlassError instances.
 */
object ErrorMapper {

    /**
     * Map a generic exception to TrailGlassError.
     */
    fun mapException(exception: Throwable): TrailGlassError {
        return when (exception) {
            // Network errors
            is UnknownHostException -> TrailGlassError.NetworkError.NoConnection(exception)
            is SocketTimeoutException,
            is TimeoutCancellationException -> TrailGlassError.NetworkError.Timeout(exception)
            is IOException -> TrailGlassError.NetworkError.RequestFailed(
                technicalMessage = exception.message ?: "IO error",
                cause = exception
            )

            // Database errors
            is app.cash.sqldelight.db.SqlDriver.Schema.MigrationException -> TrailGlassError.DatabaseError.QueryFailed(
                technicalMessage = "Database migration failed: ${exception.message}",
                cause = exception
            )

            // Validation errors
            is IllegalArgumentException -> TrailGlassError.ValidationError.InvalidInput(
                fieldName = "input",
                technicalMessage = exception.message ?: "Invalid argument"
            )
            is IllegalStateException -> TrailGlassError.ValidationError.InvalidInput(
                fieldName = "state",
                technicalMessage = exception.message ?: "Invalid state"
            )

            // Unknown
            else -> TrailGlassError.Unknown(
                technicalMessage = exception.message ?: exception::class.simpleName ?: "Unknown error",
                cause = exception
            )
        }
    }

    /**
     * Map a database exception to TrailGlassError.
     */
    fun mapDatabaseException(exception: Throwable, operation: DatabaseOperation): TrailGlassError {
        val technicalMessage = exception.message ?: "Database operation failed"

        return when (operation) {
            DatabaseOperation.QUERY -> TrailGlassError.DatabaseError.QueryFailed(
                technicalMessage = technicalMessage,
                cause = exception
            )
            DatabaseOperation.INSERT -> TrailGlassError.DatabaseError.InsertFailed(
                technicalMessage = technicalMessage,
                cause = exception
            )
            DatabaseOperation.UPDATE -> TrailGlassError.DatabaseError.UpdateFailed(
                technicalMessage = technicalMessage,
                cause = exception
            )
            DatabaseOperation.DELETE -> TrailGlassError.DatabaseError.DeleteFailed(
                technicalMessage = technicalMessage,
                cause = exception
            )
        }
    }

    /**
     * Map a location exception to TrailGlassError.
     */
    fun mapLocationException(exception: Throwable, context: LocationContext): TrailGlassError {
        return when (context) {
            LocationContext.PERMISSION -> TrailGlassError.LocationError.PermissionDenied(exception)
            LocationContext.UNAVAILABLE -> TrailGlassError.LocationError.LocationUnavailable(exception)
            LocationContext.TRACKING -> TrailGlassError.LocationError.TrackingFailed(
                technicalMessage = exception.message ?: "Tracking failed",
                cause = exception
            )
            LocationContext.GEOCODING -> TrailGlassError.LocationError.GeocodingFailed(
                technicalMessage = exception.message ?: "Geocoding failed",
                cause = exception
            )
        }
    }

    /**
     * Map a photo exception to TrailGlassError.
     */
    fun mapPhotoException(exception: Throwable, context: PhotoContext): TrailGlassError {
        return when (context) {
            PhotoContext.PERMISSION -> TrailGlassError.PhotoError.PermissionDenied(exception)
            PhotoContext.LOAD -> TrailGlassError.PhotoError.LoadFailed(
                technicalMessage = exception.message ?: "Load failed",
                cause = exception
            )
            PhotoContext.ATTACH -> TrailGlassError.PhotoError.AttachmentFailed(
                technicalMessage = exception.message ?: "Attachment failed",
                cause = exception
            )
            PhotoContext.INVALID -> TrailGlassError.PhotoError.InvalidPhoto(
                technicalMessage = exception.message ?: "Invalid photo",
                cause = exception
            )
        }
    }

    /**
     * Execute a block and map any exception to TrailGlassError.
     */
    inline fun <T> mapToResult(block: () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    /**
     * Execute a suspending block and map any exception to TrailGlassError.
     */
    suspend inline fun <T> mapToResultSuspend(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
}

/**
 * Database operation types for error mapping.
 */
enum class DatabaseOperation {
    QUERY,
    INSERT,
    UPDATE,
    DELETE
}

/**
 * Location context for error mapping.
 */
enum class LocationContext {
    PERMISSION,
    UNAVAILABLE,
    TRACKING,
    GEOCODING
}

/**
 * Photo context for error mapping.
 */
enum class PhotoContext {
    PERMISSION,
    LOAD,
    ATTACH,
    INVALID
}

/**
 * Extension function to catch and map exceptions.
 */
inline fun <T> Result<T>.catchAndMap(mapper: (Throwable) -> TrailGlassError): Result<T> {
    return when (this) {
        is Result.Success -> this
        is Result.Error -> this
    }
}

/**
 * Extension function to recover from specific errors.
 */
inline fun <T> Result<T>.recoverWith(recovery: (TrailGlassError) -> Result<T>): Result<T> {
    return when (this) {
        is Result.Success -> this
        is Result.Error -> recovery(error)
    }
}

/**
 * Extension function to recover with a default value.
 */
fun <T> Result<T>.getOrDefault(default: T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> default
    }
}

/**
 * Extension function to throw if error.
 */
fun <T> Result<T>.getOrThrow(): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> throw error.cause ?: Exception(error.technicalMessage)
    }
}
