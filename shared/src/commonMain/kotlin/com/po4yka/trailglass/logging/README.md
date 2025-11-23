# Logging Infrastructure

TrailGlass uses [kotlin-logging](https://github.com/oshai/kotlin-logging) for structured, multiplatform logging.

## Overview

Kotlin-logging provides:

- ✅ **Multiplatform support**: Works on Android, iOS, JVM, and Native
- ✅ **Lazy evaluation**: Log messages are only computed if the log level is enabled
- ✅ **Structured logging**: Lambda-based message creation
- ✅ **Platform-specific backends**: SLF4J on Android, NSLog on iOS

## Architecture

### Logging Stack

```
Application Code
      ↓
kotlin-logging (common)
      ↓
   ┌──────┴──────┐
   ↓             ↓
Android        iOS
SLF4J      NSLog/os_log
```

### Dependencies

**Version Catalog (`gradle/libs.versions.toml`)**:

- `kotlin-logging = "7.0.0"` - Multiplatform logging library
- `slf4j = "2.0.16"` - Android backend

**Shared Module**:

- `implementation(libs.kotlin.logging)` - Common logging API
- Android: `implementation(libs.slf4j.android)` - SLF4J backend for Android
- iOS: Uses NSLog/os_log natively (no additional dependencies)

## Usage

### Basic Logging

```kotlin
import com.po4yka.trailglass.logging.logger

class MyService {
    private val logger = logger() // Creates logger from class name

    fun doWork() {
        logger.info { "Starting work" }
        logger.debug { "Processing item: $item" }
        logger.error(exception) { "Failed to process" }
    }
}
```

### Named Logger

```kotlin
import com.po4yka.trailglass.logging.logger

val logger = logger("CustomFeature")

fun process() {
    logger.warn { "Custom warning message" }
}
```

### Log Levels

From most to least verbose:

1. `TRACE` - Very detailed diagnostic information
2. `DEBUG` - Detailed information for debugging
3. `INFO` - Informational messages (important events)
4. `WARN` - Warning messages (potential issues)
5. `ERROR` - Error messages (failures)

### Lazy Evaluation

**Good** - Message only computed if DEBUG is enabled:

```kotlin
logger.debug { "User ${user.id} processed ${items.size} items" }
```

**Bad** - String concatenation always happens:

```kotlin
logger.debug("User " + user.id + " processed " + items.size + " items")
```

### Exception Logging

```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    logger.error(e) { "Operation failed for user ${userId}" }
}
```

## Platform-Specific Configuration

### Android

**Backend**: SLF4J-Android (logs to Android Logcat)

**Log Tags**: Automatically derived from class name

- `LocationRepositoryImpl` → Tag: `com.po4yka.trailglass.data.repository.impl.LocationRepositoryImpl`

**Filtering in Logcat**:

```bash
# Filter by package
adb logcat | grep "com.po4yka.trailglass"

# Filter by tag
adb logcat -s "LocationRepositoryImpl:*"

# Filter by level
adb logcat *:E  # Errors only
adb logcat *:D  # Debug and above
```

**Log Level Configuration**:
SLF4J-Android uses Android's log levels. No additional configuration needed.

### iOS

**Backend**: NSLog (development) / os_log (production)

**Log Output**: Visible in Xcode Console

**Filtering in Xcode**:

- Use the filter field in the console
- Search for class names or log messages
- Filter by subsystem (if configured)

**Log Level**: Controlled by OS logging settings. In development, all levels are visible.

## Logging Best Practices

### What to Log

**✅ DO Log:**

- Important state changes
- External API calls (geocoding, network)
- Database operations (with counts, not full data)
- Processing milestones (e.g., "Detected 5 place visits from 100 samples")
- Errors and exceptions
- Performance-critical operations

**❌ DON'T Log:**

- Sensitive user data (coordinates at TRACE level only)
- Passwords, tokens, API keys
- Full database records
- High-frequency operations in hot paths

### Log Level Guidelines

**TRACE**: Fine-grained diagnostic

```kotlin
logger.trace { "Looking up cache for ($lat, $lon)" }
logger.trace { "Successfully inserted sample ${sample.id}" }
```

**DEBUG**: Detailed debugging information

```kotlin
logger.debug { "Inserting location sample: ${sample.id} at ($lat, $lon)" }
logger.debug { "Found ${samples.size} location samples" }
logger.debug { "Cache HIT for coordinates" }
```

**INFO**: Important business events

```kotlin
logger.info { "Processing ${samples.size} location samples" }
logger.info { "Detected ${visits.size} place visits" }
logger.info { "Geocoded place visit: ${city} (${countryCode})" }
```

**WARN**: Potential issues

```kotlin
logger.warn { "Failed to geocode place visit at ($lat, $lon)" }
logger.warn { "Android Geocoder is not present on device" }
logger.warn { "Cache cleanup took ${duration}ms" }
```

**ERROR**: Failures and exceptions

```kotlin
logger.error(e) { "Failed to insert location sample ${id}" }
logger.error(e) { "Database operation failed" }
logger.error { "Critical: Unable to initialize geocoder" }
```

### Performance Tips

1. **Always use lambdas** for message construction:
   ```kotlin
   logger.debug { "Expensive: ${computeExpensive()}" } // ✅ Only computed if DEBUG enabled
   ```

2. **Avoid string concatenation** outside lambdas:
   ```kotlin
   val msg = "User: $userId"  // ❌ Always computed
   logger.debug(msg)
   ```

3. **Minimize TRACE logging** in production builds

4. **Use structured messages** for better filtering:
   ```kotlin
   logger.info { "operation=geocoding, lat=$lat, lon=$lon, result=${result}" }
   ```

## Examples from TrailGlass

### Repository Logging

```kotlin
class LocationRepositoryImpl(private val database: Database) {
    private val logger = logger()

    override suspend fun insertSample(sample: LocationSample) {
        logger.debug { "Inserting location sample: ${sample.id}" }
        try {
            // ... database operation
            logger.trace { "Successfully inserted sample ${sample.id}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to insert sample ${sample.id}" }
            throw e
        }
    }

    override suspend fun getSamples(...): List<LocationSample> {
        logger.debug { "Fetching samples for user $userId" }
        val samples = // ... query
        logger.info { "Found ${samples.size} location samples" }
        return samples
    }
}
```

### Service Logging

```kotlin
class PlaceVisitProcessor(...) {
    private val logger = logger()

    suspend fun detectPlaceVisits(samples: List<LocationSample>): List<PlaceVisit> {
        logger.info { "Processing ${samples.size} samples to detect visits" }

        val clusters = clusterSamples(samples)
        logger.debug { "Found ${clusters.size} clusters" }

        val visits = clusters.mapNotNull { createPlaceVisit(it) }
        logger.info { "Detected ${visits.size} place visits" }

        return visits
    }
}
```

### Geocoding Logging

```kotlin
class GeocodingCacheRepositoryImpl(...) {
    private val logger = logger()

    override suspend fun get(...): GeocodedLocation? {
        logger.trace { "Looking up cache for ($lat, $lon)" }
        val result = // ... query

        if (result != null) {
            logger.debug { "Cache HIT: ${result.city}" }
        } else {
            logger.trace { "Cache MISS" }
        }
        return result
    }
}
```

## Troubleshooting

### Android: Logs not appearing

1. Check Logcat filter settings
2. Verify log level (Debug builds show all levels)
3. Check ProGuard/R8 rules (shouldn't strip logging)

### iOS: Logs not appearing

1. Check Xcode Console is visible
2. Verify device vs simulator logging
3. Check Console app on macOS for device logs

### Performance Impact

kotlin-logging has minimal overhead:

- Lambda evaluation is skipped if log level disabled
- No string concatenation unless needed
- Platform backends are optimized

## Migration Guide

### From println/System.out

**Before**:

```kotlin
println("Processing ${items.size} items")
System.out.println("Error: $message")
```

**After**:

```kotlin
private val logger = logger()

logger.info { "Processing ${items.size} items" }
logger.error { "Error: $message" }
```

### From Android Log

**Before**:

```kotlin
Log.d(TAG, "Debug message")
Log.e(TAG, "Error message", exception)
```

**After**:

```kotlin
private val logger = logger()

logger.debug { "Debug message" }
logger.error(exception) { "Error message" }
```

## Future Enhancements

Planned improvements:

- [ ] Structured logging with JSON format
- [ ] Log file rotation for offline debugging
- [ ] Remote logging for production monitoring
- [ ] Performance metrics integration
- [ ] Custom log levels per module

## Resources

- [kotlin-logging GitHub](https://github.com/oshai/kotlin-logging)
- [SLF4J Documentation](http://www.slf4j.org/)
- [Apple Logging Documentation](https://developer.apple.com/documentation/os/logging)

---

**See Also**:

- [data/README.md](../data/README.md) - Database layer with logging examples
- [location/README.md](../location/README.md) - Location services with logging
