# Location & Reverse Geocoding Infrastructure

This module provides location tracking and reverse geocoding capabilities for TrailGlass using Kotlin Multiplatform.

## Architecture

### Core Components

1. **Domain Models** (`domain/model/`)
   - `LocationSample`: Raw GPS/network location points
   - `PlaceVisit`: Clustered locations where user stayed for significant time
   - `GeocodedLocation`: Reverse geocoding results with address components

2. **Reverse Geocoding** (`location/geocoding/`)
   - `ReverseGeocoder`: Platform-agnostic interface for reverse geocoding
   - `AndroidReverseGeocoder`: Android implementation using `Geocoder` API
   - `IOSReverseGeocoder`: iOS implementation using `CLGeocoder`
   - `GeocodingCache`: In-memory cache with spatial proximity matching
   - `CachedReverseGeocoder`: Wrapper that adds caching to any `ReverseGeocoder`

3. **Location Processing** (`location/`)
   - `LocationRecorder`: Interface for recording location samples
   - `PlaceVisitProcessor`: Detects place visits using DBSCAN-like clustering
   - `LocationService`: Central service integrating location recording and geocoding

## Usage

### Android Setup

```kotlin
import com.po4yka.trailglass.location.LocationService
import com.po4yka.trailglass.location.geocoding.createAndroidReverseGeocoder

// Create the location service with Android geocoder
val geocoder = createAndroidReverseGeocoder(context)
val locationService = LocationService(geocoder)

// Record location samples
val sample = LocationSample(
    id = "sample_1",
    timestamp = Clock.System.now(),
    latitude = 41.7151,
    longitude = 44.8271,
    accuracy = 10.0,
    source = LocationSource.GPS,
    deviceId = "device_1",
    userId = "user_1"
)

locationService.recordSample(sample)

// Process samples to detect place visits (with reverse geocoding)
locationService.processSamples()

// Get detected place visits
val visits = locationService.getPlaceVisits()
visits.forEach { visit ->
    println("Visit at ${visit.city}, ${visit.countryCode}")
    println("POI: ${visit.poiName}")
    println("Address: ${visit.approximateAddress}")
}
```

### iOS Setup

```kotlin
import com.po4yka.trailglass.location.LocationService
import com.po4yka.trailglass.location.geocoding.createReverseGeocoder

// Create the location service with iOS geocoder
val geocoder = createReverseGeocoder()
val locationService = LocationService(geocoder)

// Use the same API as Android
```

## Features

### Reverse Geocoding

The reverse geocoding infrastructure provides:

- **Platform-specific implementations**: Uses native APIs on each platform
  - Android: `Geocoder` API with support for both legacy and modern (API 33+) methods
  - iOS: `CLGeocoder` from CoreLocation framework

- **Intelligent caching**:
  - Spatial proximity matching (default: 100m radius)
  - Time-based expiration (default: 30 days)
  - Reduces API calls and improves performance

- **Comprehensive address data**:
  - Formatted address
  - City, state, country (code and name)
  - POI (Point of Interest) name
  - Street address components
  - Postal code

### Place Visit Detection

The `PlaceVisitProcessor` uses clustering algorithms to:

1. Group nearby location samples (within configurable spatial threshold)
2. Filter by minimum duration (default: 10 minutes)
3. Calculate center point for each cluster
4. Perform reverse geocoding to enrich with address information

### Caching Strategy

The `GeocodingCache` implements:

- **Spatial proximity matching**: Returns cached results for nearby coordinates
- **Haversine distance calculation**: Accurate distance computation on Earth's surface
- **Automatic expiration**: Removes stale entries based on time
- **Memory-efficient**: In-memory cache suitable for mobile devices

## Configuration

### PlaceVisitProcessor Parameters

```kotlin
PlaceVisitProcessor(
    reverseGeocoder = geocoder,
    minDurationThreshold = 10.minutes,  // Minimum time at location
    spatialThresholdMeters = 100.0      // Clustering radius in meters
)
```

### GeocodingCache Parameters

```kotlin
GeocodingCache(
    proximityThresholdMeters = 100.0,   // Cache hit radius
    cacheDuration = 30.days             // Cache entry lifetime
)
```

## Integration with Location Workflow

The infrastructure is designed to integrate seamlessly into the location tracking workflow:

1. **Recording**: Platform-specific code captures location updates
2. **Storage**: Samples are stored via `LocationRecorder.recordSample()`
3. **Processing**: Periodic processing clusters samples into visits
4. **Geocoding**: Each visit is reverse geocoded with caching
5. **Output**: Enriched `PlaceVisit` objects with address information

## Future Enhancements

Planned improvements (from TODO.md Phase 2):

- [ ] Database persistence for geocoding cache (SQLDelight)
- [ ] Country boundary detection for automatic country visits
- [ ] POI name detection with confidence scoring
- [ ] Rate limiting for geocoding API calls
- [ ] Offline geocoding with bundled city database
- [ ] Custom geocoding providers (Mapbox, Google Places, etc.)

## See Also

- [TODO.md](../../../TODO.md) - Phase 2: Reverse Geocoding tasks
- [SPEC.md](../../../SPEC.md) - Section 2.2: PlaceVisit specification
- [ROADMAP.md](../../../ROADMAP.md) - Month 4: Photo & Reverse Geocoding
