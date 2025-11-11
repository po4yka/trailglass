package com.po4yka.trailglass.domain.error

/**
 * Maps platform-specific exceptions to TrailGlassError instances.
 * Platform-specific implementations handle their own exception types.
 */
expect object ErrorMapper {

    /**
     * Map a generic exception to TrailGlassError.
     */
    fun mapException(exception: Throwable): TrailGlassError

    /**
     * Map a database exception to TrailGlassError.
     */
    fun mapDatabaseException(exception: Throwable, operation: DatabaseOperation): TrailGlassError

    /**
     * Map a location exception to TrailGlassError.
     */
    fun mapLocationException(exception: Throwable, context: LocationContext): TrailGlassError

    /**
     * Map a photo exception to TrailGlassError.
     */
    fun mapPhotoException(exception: Throwable, context: PhotoContext): TrailGlassError

    /**
     * Execute a block and map any exception to TrailGlassError.
     */
    fun <T> mapToResult(block: () -> T): Result<T>

    /**
     * Execute a suspending block and map any exception to TrailGlassError.
     */
    suspend fun <T> mapToResultSuspend(block: suspend () -> T): Result<T>
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
