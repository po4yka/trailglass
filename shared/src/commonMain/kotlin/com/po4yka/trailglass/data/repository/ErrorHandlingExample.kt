package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.data.analytics.ErrorAnalytics
import com.po4yka.trailglass.data.analytics.ErrorSeverity
import com.po4yka.trailglass.data.analytics.logToAnalytics
import com.po4yka.trailglass.data.network.NetworkConnectivity
import com.po4yka.trailglass.data.network.RetryPolicy
import com.po4yka.trailglass.data.network.retryWithPolicy
import com.po4yka.trailglass.domain.error.*
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Example repository implementation demonstrating proper error handling.
 *
 * This serves as a template for implementing error handling across repositories.
 */
class PlaceVisitRepositoryWithErrorHandling(
    private val database: Any, // Replace with actual database type
    private val networkConnectivity: NetworkConnectivity,
    private val errorAnalytics: ErrorAnalytics
) {
    private val logger = logger()

    /**
     * Get place visit by ID with proper error handling.
     *
     * Example of:
     * - Error mapping from exceptions
     * - Analytics logging
     * - User-friendly error messages
     */
    suspend fun getPlaceVisitById(id: String, userId: String): Result<PlaceVisit?> {
        return ErrorMapper.mapToResultSuspend {
            try {
                // Simulate database query
                // val visit = database.placeVisitsQueries.getById(id, userId).executeAsOneOrNull()
                // visit?.toDomainModel()
                null
            } catch (e: Exception) {
                val error = ErrorMapper.mapDatabaseException(e, DatabaseOperation.QUERY)
                error.logToAnalytics(
                    errorAnalytics,
                    context = mapOf(
                        "operation" to "getPlaceVisitById",
                        "visitId" to id,
                        "userId" to userId
                    )
                )
                throw e
            }
        }
    }

    /**
     * Get place visits for time range with retry logic.
     *
     * Example of:
     * - Retry mechanism with exponential backoff
     * - Flow-based error handling
     * - Network connectivity awareness
     */
    fun getPlaceVisitsForTimeRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<Result<List<PlaceVisit>>> {
        return kotlinx.coroutines.flow.flow {
            // Execute with retry
            val result = retryWithPolicy(
                policy = RetryPolicy.DEFAULT,
                onRetry = { state ->
                    logger.info {
                        "Retrying getPlaceVisitsForTimeRange (attempt ${state.attempt})"
                    }
                }
            ) {
                try {
                    // Simulate database query
                    // val visits = database.placeVisitsQueries
                    //     .getForTimeRange(userId, startTime, endTime)
                    //     .executeAsList()
                    //     .map { it.toDomainModel() }
                    Result.Success(emptyList<PlaceVisit>())
                } catch (e: Exception) {
                    val error = ErrorMapper.mapDatabaseException(e, DatabaseOperation.QUERY)
                    error.logToAnalytics(
                        errorAnalytics,
                        context = mapOf(
                            "operation" to "getPlaceVisitsForTimeRange",
                            "userId" to userId,
                            "startTime" to startTime.toString(),
                            "endTime" to endTime.toString()
                        )
                    )
                    Result.Error(error)
                }
            }

            emit(result)
        }
    }

    /**
     * Insert place visit with validation and error handling.
     *
     * Example of:
     * - Input validation
     * - Constraint violation handling
     * - Offline mode support
     */
    suspend fun insertPlaceVisit(visit: PlaceVisit): Result<Unit> {
        // Validate input
        val validationResult = validatePlaceVisit(visit)
        if (validationResult is Result.Error) {
            return validationResult
        }

        // Check network if needed for sync
        val isOnline = networkConnectivity.isNetworkAvailable()
        if (!isOnline) {
            logger.info { "Inserting place visit in offline mode" }
        }

        return try {
            // Simulate database insert
            // database.placeVisitsQueries.insert(visit.toDbModel())
            Result.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorMapper.mapDatabaseException(e, DatabaseOperation.INSERT)

            // Check if constraint violation
            if (e.message?.contains("UNIQUE constraint failed") == true) {
                TrailGlassError.DatabaseError.ConstraintViolation(
                    technicalMessage = "Place visit already exists",
                    cause = e
                ).let {
                    it.logToAnalytics(
                        errorAnalytics,
                        context = mapOf(
                            "operation" to "insertPlaceVisit",
                            "visitId" to visit.id
                        ),
                        severity = ErrorSeverity.WARNING
                    )
                    Result.Error(it)
                }
            } else {
                error.logToAnalytics(
                    errorAnalytics,
                    context = mapOf(
                        "operation" to "insertPlaceVisit",
                        "visitId" to visit.id
                    )
                )
                Result.Error(error)
            }
        }
    }

    /**
     * Update place visit with retry and validation.
     *
     * Example of:
     * - Retry on transient failures
     * - Validation before update
     * - Proper error recovery
     */
    suspend fun updatePlaceVisit(visit: PlaceVisit): Result<Unit> {
        // Validate
        val validationResult = validatePlaceVisit(visit)
        if (validationResult is Result.Error) {
            return validationResult
        }

        // Update with retry
        return retryWithPolicy(
            policy = RetryPolicy.CONSERVATIVE,
            onRetry = { state ->
                logger.warn {
                    "Retrying updatePlaceVisit for ${visit.id} (attempt ${state.attempt})"
                }
            }
        ) {
            try {
                // Simulate database update
                // database.placeVisitsQueries.update(visit.toDbModel())
                Result.Success(Unit)
            } catch (e: Exception) {
                val error = ErrorMapper.mapDatabaseException(e, DatabaseOperation.UPDATE)
                error.logToAnalytics(
                    errorAnalytics,
                    context = mapOf(
                        "operation" to "updatePlaceVisit",
                        "visitId" to visit.id
                    )
                )
                Result.Error(error)
            }
        }
    }

    /**
     * Delete place visit with cascade handling.
     *
     * Example of:
     * - Cascade delete error handling
     * - Foreign key constraint handling
     */
    suspend fun deletePlaceVisit(id: String, userId: String): Result<Unit> {
        return try {
            // Simulate database delete
            // database.placeVisitsQueries.delete(id, userId)
            Result.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorMapper.mapDatabaseException(e, DatabaseOperation.DELETE)
            error.logToAnalytics(
                errorAnalytics,
                context = mapOf(
                    "operation" to "deletePlaceVisit",
                    "visitId" to id,
                    "userId" to userId
                )
            )
            Result.Error(error)
        }
    }

    /**
     * Validate place visit input.
     */
    private fun validatePlaceVisit(visit: PlaceVisit): Result<Unit> {
        // Validate coordinates
        if (visit.centerLatitude < -90 || visit.centerLatitude > 90) {
            return Result.Error(
                TrailGlassError.ValidationError.InvalidCoordinate(
                    technicalMessage = "Latitude ${visit.centerLatitude} out of range [-90, 90]"
                )
            )
        }

        if (visit.centerLongitude < -180 || visit.centerLongitude > 180) {
            return Result.Error(
                TrailGlassError.ValidationError.InvalidCoordinate(
                    technicalMessage = "Longitude ${visit.centerLongitude} out of range [-180, 180]"
                )
            )
        }

        // Validate date range
        if (visit.endTime < visit.startTime) {
            return Result.Error(
                TrailGlassError.ValidationError.InvalidDateRange(
                    technicalMessage = "End time ${visit.endTime} before start time ${visit.startTime}"
                )
            )
        }

        return Result.Success(Unit)
    }
}

/**
 * Extension function demonstrating Flow error handling.
 */
fun <T> Flow<T>.handleErrors(errorAnalytics: ErrorAnalytics): Flow<Result<T>> {
    return this
        .map { Result.Success(it) as Result<T> }
        .catch { exception ->
            val error = ErrorMapper.mapException(exception)
            error.logToAnalytics(errorAnalytics)
            emit(Result.Error(error))
        }
}
