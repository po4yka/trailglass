# Error Handling

Comprehensive error handling guide for TrailGlass with user-friendly messages, offline mode support, retry mechanisms, and analytics.

## Table of Contents

- [Overview](#overview)
- [Error Types](#error-types)
- [Error Handling Patterns](#error-handling-patterns)
- [Network Connectivity](#network-connectivity)
- [Retry Mechanisms](#retry-mechanisms)
- [Error Analytics](#error-analytics)
- [Best Practices](#best-practices)
- [Examples](#examples)

## Overview

TrailGlass uses a comprehensive error handling system that provides:

- **User-Friendly Messages**: No raw exceptions shown to users
- **Offline Mode Support**: Graceful handling of network unavailability
- **Retry Mechanisms**: Automatic retry with exponential backoff
- **Error Analytics**: Structured error logging and reporting
- **Type Safety**: Compile-time error handling with Result type

### Architecture

```
Exception
    ↓
ErrorMapper
    ↓
TrailGlassError (sealed class)
    ↓
Result<T> (Success | Error)
    ↓
User-Friendly Message
    ↓
ErrorAnalytics
```

## Error Types

All errors inherit from `TrailGlassError` sealed class.

### Network Errors

```kotlin
sealed class NetworkError : TrailGlassError {
    data class NoConnection(cause: Throwable?)
    data class Timeout(cause: Throwable?)
    data class ServerError(statusCode: Int, cause: Throwable?)
    data class RequestFailed(technicalMessage: String, cause: Throwable?)
}
```

**User Messages**:
- No Connection: "No internet connection. Please check your network settings."
- Timeout: "Request timed out. Please try again."
- Server Error: "Server error occurred. Please try again later."

**Error Codes**: NET_001, NET_002, NET_003, NET_004

### Database Errors

```kotlin
sealed class DatabaseError : TrailGlassError {
    data class QueryFailed(technicalMessage: String, cause: Throwable?)
    data class InsertFailed(technicalMessage: String, cause: Throwable?)
    data class UpdateFailed(technicalMessage: String, cause: Throwable?)
    data class DeleteFailed(technicalMessage: String, cause: Throwable?)
    data class ConstraintViolation(technicalMessage: String, cause: Throwable?)
}
```

**User Messages**:
- Query Failed: "Failed to load data. Please try again."
- Insert Failed: "Failed to save data. Please try again."
- Update Failed: "Failed to update data. Please try again."

**Error Codes**: DB_001, DB_002, DB_003, DB_004, DB_005

### Location Errors

```kotlin
sealed class LocationError : TrailGlassError {
    data class PermissionDenied(cause: Throwable?)
    data class LocationUnavailable(cause: Throwable?)
    data class TrackingFailed(technicalMessage: String, cause: Throwable?)
    data class GeocodingFailed(technicalMessage: String, cause: Throwable?)
}
```

**User Messages**:
- Permission Denied: "Location permission required. Please grant location access in settings."
- Location Unavailable: "Location unavailable. Please ensure location services are enabled."

**Error Codes**: LOC_001, LOC_002, LOC_003, LOC_004

### Photo Errors

```kotlin
sealed class PhotoError : TrailGlassError {
    data class PermissionDenied(cause: Throwable?)
    data class LoadFailed(technicalMessage: String, cause: Throwable?)
    data class AttachmentFailed(technicalMessage: String, cause: Throwable?)
    data class InvalidPhoto(technicalMessage: String, cause: Throwable?)
}
```

**User Messages**:
- Permission Denied: "Photo library access required. Please grant permission in settings."
- Load Failed: "Failed to load photo. Please try again."

**Error Codes**: PHOTO_001, PHOTO_002, PHOTO_003, PHOTO_004

### Validation Errors

```kotlin
sealed class ValidationError : TrailGlassError {
    data class InvalidInput(fieldName: String, technicalMessage: String)
    data class RequiredFieldMissing(fieldName: String)
    data class InvalidCoordinate(technicalMessage: String)
    data class InvalidDateRange(technicalMessage: String)
}
```

**User Messages**:
- Invalid Input: "Invalid {fieldName}. Please check your input."
- Required Field Missing: "{fieldName} is required."

**Error Codes**: VAL_001, VAL_002, VAL_003, VAL_004

## Error Handling Patterns

### Result Type

Use `Result<T>` for all operations that can fail:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: TrailGlassError) : Result<Nothing>()
}
```

**Usage**:

```kotlin
// Return Result from repository
suspend fun getPlaceVisit(id: String): Result<PlaceVisit?> {
    return try {
        val visit = database.placeVisitsQueries.getById(id).executeAsOneOrNull()
        Result.Success(visit)
    } catch (e: Exception) {
        Result.Error(ErrorMapper.mapDatabaseException(e, DatabaseOperation.QUERY))
    }
}

// Handle Result in controller
when (val result = repository.getPlaceVisit(id)) {
    is Result.Success -> {
        _state.update { it.copy(visit = result.data) }
    }
    is Result.Error -> {
        _state.update { it.copy(error = result.error.userMessage) }
        result.error.logToAnalytics(errorAnalytics)
    }
}
```

### Result Extensions

```kotlin
// Map successful result
result.map { visit -> visit.toUI() }

// FlatMap for chaining
result.flatMap { visit -> loadPhotosForVisit(visit.id) }

// Get value or default
val visit = result.getOrDefault(defaultVisit)

// Execute on success
result.onSuccess { visit ->
    println("Loaded visit: ${visit.city}")
}

// Execute on error
result.onError { error ->
    println("Error: ${error.userMessage}")
}
```

### Error Mapper

Map exceptions to TrailGlassError:

```kotlin
// Automatic mapping
val result = ErrorMapper.mapToResult {
    database.placeVisitsQueries.getById(id).executeAsOneOrNull()
}

// Suspending version
val result = ErrorMapper.mapToResultSuspend {
    networkClient.fetchData()
}

// Context-specific mapping
val error = ErrorMapper.mapLocationException(
    exception = e,
    context = LocationContext.GEOCODING
)
```

## Network Connectivity

### NetworkConnectivity Interface

```kotlin
interface NetworkConnectivity {
    val isConnected: Flow<Boolean>
    suspend fun isNetworkAvailable(): Boolean
    suspend fun getNetworkType(): NetworkType
}
```

### Usage

```kotlin
// Create connectivity monitor
val connectivity = NetworkConnectivityFactory.create()

// Check current status
if (connectivity.isNetworkAvailable()) {
    // Perform network operation
} else {
    return Result.Error(TrailGlassError.NetworkError.NoConnection())
}

// Observe connectivity changes
connectivity.isConnected.collect { isConnected ->
    if (isConnected) {
        // Sync pending data
    } else {
        // Queue operations for later
    }
}

// Check network type
when (connectivity.getNetworkType()) {
    NetworkType.WIFI -> // Upload photos
    NetworkType.CELLULAR -> // Sync metadata only
    NetworkType.NONE -> // Offline mode
    else -> // Default behavior
}
```

### Offline Mode

```kotlin
suspend fun syncData(): Result<Unit> {
    // Check connectivity first
    if (!connectivity.isNetworkAvailable()) {
        logger.info { "Operating in offline mode" }
        return Result.Error(TrailGlassError.NetworkError.NoConnection())
    }

    // Proceed with sync
    return performSync()
}
```

## Retry Mechanisms

### Retry Policy

```kotlin
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = 1.seconds,
    val maxDelay: Duration = 30.seconds,
    val multiplier: Double = 2.0
)
```

### Predefined Policies

```kotlin
// Default: 3 attempts, 1s initial delay, 2x multiplier
RetryPolicy.DEFAULT

// Aggressive: 5 attempts, 500ms initial, 1.5x multiplier
RetryPolicy.AGGRESSIVE

// Conservative: 2 attempts, 2s initial, 2x multiplier
RetryPolicy.CONSERVATIVE

// Network-specific: Retries network errors only
RetryPolicy.NETWORK

// No retry
RetryPolicy.NONE
```

### Usage

```kotlin
// Basic retry
val result = retryWithPolicy(
    policy = RetryPolicy.DEFAULT
) {
    performOperation()
}

// With retry callback
val result = retryWithPolicy(
    policy = RetryPolicy.AGGRESSIVE,
    onRetry = { state ->
        logger.info { "Retry attempt ${state.attempt}" }
        updateUI("Retrying... (${state.attempt}/${policy.maxAttempts})")
    }
) {
    performNetworkRequest()
}

// Network-aware retry
val result = retryWithNetwork(
    policy = RetryPolicy.NETWORK,
    networkConnectivity = connectivity,
    onRetry = { state ->
        showRetryToast("Retrying...")
    }
) {
    syncData()
}
```

### Exponential Backoff

Delays between retries:
- Attempt 1: 1s
- Attempt 2: 2s (1s × 2.0)
- Attempt 3: 4s (2s × 2.0)
- Attempt 4: 8s (4s × 2.0)
- Maximum: 30s (capped)

### Custom Retry Logic

```kotlin
val customPolicy = RetryPolicy(
    maxAttempts = 4,
    initialDelay = 500.milliseconds,
    shouldRetry = { error ->
        // Only retry on specific errors
        error is TrailGlassError.NetworkError.Timeout ||
        error is TrailGlassError.DatabaseError.QueryFailed
    }
)
```

## Error Analytics

### ErrorAnalytics Interface

```kotlin
interface ErrorAnalytics {
    fun logError(error: TrailGlassError, context: Map<String, Any>)
    fun logNonFatal(error: TrailGlassError, context: Map<String, Any>)
    fun logFatal(error: TrailGlassError, context: Map<String, Any>)
    fun setUserId(userId: String?)
    fun setCustomData(key: String, value: String)
}
```

### Usage

```kotlin
// Create analytics
val analytics = ErrorAnalyticsFactory.create()

// Set user ID
analytics.setUserId(currentUserId)

// Add custom context
analytics.setCustomData("app_version", "1.0.0")
analytics.setCustomData("device_type", "pixel_5")

// Log errors
analytics.logError(
    error = error,
    context = mapOf(
        "operation" to "loadPlaceVisits",
        "userId" to userId,
        "startTime" to startTime.toString()
    )
)

// Extension function
error.logToAnalytics(
    analytics,
    context = mapOf("screen" to "timeline"),
    severity = ErrorSeverity.ERROR
)
```

### Error Severity

```kotlin
enum class ErrorSeverity {
    INFO,      // Informational
    WARNING,   // Potential problems
    ERROR,     // Functional errors
    FATAL      // Critical errors
}
```

### Integration with Firebase Crashlytics

```kotlin
// Android (commented example in ErrorAnalytics.android.kt)
class FirebaseCrashlyticsErrorAnalytics : ErrorAnalytics {
    private val crashlytics = Firebase.crashlytics

    override fun logError(error: TrailGlassError, context: Map<String, Any>) {
        crashlytics.recordException(
            Exception(error.userMessage, error.cause)
        )
        context.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value.toString())
        }
    }
}
```

## Best Practices

### 1. Always Use Result Type

```kotlin
// Good
suspend fun loadData(): Result<Data>

// Bad
suspend fun loadData(): Data  // Can throw exception
```

### 2. Map Exceptions Early

```kotlin
// Good - Map at repository layer
suspend fun getData(): Result<Data> {
    return try {
        Result.Success(database.query())
    } catch (e: Exception) {
        Result.Error(ErrorMapper.mapException(e))
    }
}

// Bad - Let exceptions propagate
suspend fun getData(): Data {
    return database.query()  // Throws
}
```

### 3. Provide Context to Errors

```kotlin
// Good
error.logToAnalytics(
    analytics,
    context = mapOf(
        "operation" to "syncPhotos",
        "photoCount" to photos.size,
        "userId" to userId
    )
)

// Bad
error.logToAnalytics(analytics)  // No context
```

### 4. Use Appropriate Retry Policies

```kotlin
// Good - Critical operation
retryWithPolicy(RetryPolicy.AGGRESSIVE) {
    saveTripData(trip)
}

// Good - Non-critical operation
retryWithPolicy(RetryPolicy.CONSERVATIVE) {
    loadStatistics()
}

// Bad - Always aggressive
retryWithPolicy(RetryPolicy.AGGRESSIVE) {
    loadOptionalData()  // Not critical
}
```

### 5. Handle Offline Mode Explicitly

```kotlin
// Good
if (!connectivity.isNetworkAvailable()) {
    // Queue for later sync
    queuePendingOperation(operation)
    return Result.Success(Unit)  // Graceful degradation
}

// Bad
if (!connectivity.isNetworkAvailable()) {
    throw Exception("No connection")  // User sees error
}
```

### 6. Display User-Friendly Messages

```kotlin
// Good
when (result) {
    is Result.Error -> {
        showSnackbar(result.error.getUserFriendlyMessage())
        logger.error { result.error.getTechnicalDetails() }
    }
}

// Bad
when (result) {
    is Result.Error -> {
        showSnackbar(result.error.cause?.stackTraceToString())  // Technical message to user
    }
}
```

### 7. Validate Input Early

```kotlin
// Good
fun updateVisit(visit: PlaceVisit): Result<Unit> {
    // Validate first
    if (visit.latitude !in -90.0..90.0) {
        return Result.Error(
            TrailGlassError.ValidationError.InvalidCoordinate("Invalid latitude")
        )
    }

    // Then update
    return database.update(visit)
}
```

### 8. Log with Appropriate Severity

```kotlin
// Info - Expected conditions
logger.info { "Operating in offline mode" }

// Warning - Potential issues
logger.warn { "Retry attempt ${attempt}" }

// Error - Functional errors
logger.error { "Failed to sync data: ${error.message}" }
```

## Examples

### Repository with Error Handling

```kotlin
class PlaceVisitRepository(
    private val database: TrailGlassDatabase,
    private val connectivity: NetworkConnectivity,
    private val analytics: ErrorAnalytics
) {
    suspend fun getPlaceVisit(id: String, userId: String): Result<PlaceVisit?> {
        return retryWithPolicy(RetryPolicy.DEFAULT) {
            try {
                val visit = database.placeVisitsQueries
                    .getById(id, userId)
                    .executeAsOneOrNull()
                Result.Success(visit?.toDomainModel())
            } catch (e: Exception) {
                val error = ErrorMapper.mapDatabaseException(e, DatabaseOperation.QUERY)
                error.logToAnalytics(
                    analytics,
                    context = mapOf("operation" to "getPlaceVisit", "id" to id)
                )
                Result.Error(error)
            }
        }
    }

    suspend fun syncVisits(): Result<Unit> {
        // Check network
        if (!connectivity.isNetworkAvailable()) {
            return Result.Error(TrailGlassError.NetworkError.NoConnection())
        }

        // Sync with retry
        return retryWithNetwork(
            policy = RetryPolicy.NETWORK,
            networkConnectivity = connectivity,
            onRetry = { state ->
                logger.info { "Retrying sync (attempt ${state.attempt})" }
            }
        ) {
            try {
                // Perform sync
                performNetworkSync()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(ErrorMapper.mapException(e))
            }
        }
    }
}
```

### Controller with Error Handling

```kotlin
class TimelineController(
    private val repository: PlaceVisitRepository,
    private val analytics: ErrorAnalytics
) {
    data class State(
        val visits: List<PlaceVisit> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isRetrying: Boolean = false,
        val retryAttempt: Int = 0
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun loadVisits(userId: String, date: LocalDate) {
        _state.update { it.copy(isLoading = true, error = null) }

        coroutineScope.launch {
            val result = repository.getVisitsForDay(userId, date)

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            visits = result.data,
                            isLoading = false,
                            isRetrying = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            error = result.error.getUserFriendlyMessage(),
                            isLoading = false,
                            isRetrying = false
                        )
                    }

                    // Log for analytics
                    result.error.logToAnalytics(
                        analytics,
                        context = mapOf(
                            "screen" to "timeline",
                            "operation" to "loadVisits",
                            "date" to date.toString()
                        )
                    )
                }
            }
        }
    }

    fun retry() {
        val currentState = _state.value
        _state.update {
            it.copy(
                isRetrying = true,
                retryAttempt = it.retryAttempt + 1,
                error = null
            )
        }

        // Retry last operation
        // ...
    }
}
```

### UI Error Display

```kotlin
@Composable
fun TimelineScreen(controller: TimelineController) {
    val state by controller.state.collectAsState()

    if (state.error != null) {
        Snackbar(
            action = {
                TextButton(onClick = { controller.retry() }) {
                    Text("Retry")
                }
            }
        ) {
            Text(state.error!!)
        }
    }

    if (state.isRetrying) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
        Text("Retrying (${state.retryAttempt}/3)...")
    }
}
```

## Related Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture
- [TESTING.md](TESTING.md) - Testing error handling
- [DEVELOPMENT.md](DEVELOPMENT.md) - Debugging errors

## Summary

TrailGlass error handling provides:

✅ User-friendly error messages
✅ Offline mode support with graceful degradation
✅ Automatic retry with exponential backoff
✅ Structured error analytics and logging
✅ Type-safe error handling with Result
✅ Platform-specific network connectivity monitoring
✅ Context-aware error mapping
✅ Comprehensive error categorization

Follow these patterns consistently across the codebase for robust error handling.
