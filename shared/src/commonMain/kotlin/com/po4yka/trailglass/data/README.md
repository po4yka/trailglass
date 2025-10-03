# Data Layer - Database & Repositories

This module provides persistent storage for TrailGlass using SQLDelight with cross-platform support.

## Architecture

### Database Layer (`data/db/`)

**SQLDelight Schema** (`sqldelight/com/po4yka/trailglass/db/`)
- `LocationSamples.sq`: Raw location points storage with indices
- `PlaceVisits.sq`: Place visits and sample linkage
- `GeocodingCache.sq`: Persistent geocoding cache

**Database Wrapper**
- `Database.kt`: Main database access class
- `DatabaseDriverFactory.kt`: Platform-specific driver factory (expect/actual)
  - `DatabaseDriverFactory.android.kt`: Android SQLite driver
  - `DatabaseDriverFactory.ios.kt`: iOS native driver

### Repository Layer (`data/repository/`)

**Interfaces**
- `LocationRepository`: CRUD operations for location samples
- `PlaceVisitRepository`: CRUD operations for place visits
- `GeocodingCacheRepository`: Spatial cache queries

**Implementations** (`data/repository/impl/`)
- `LocationRepositoryImpl`: SQLDelight implementation
- `PlaceVisitRepositoryImpl`: SQLDelight implementation with sample linkage
- `GeocodingCacheRepositoryImpl`: Spatial queries with Haversine distance

## Features

### Persistent Geocoding Cache

The geocoding cache now persists across app restarts:

```kotlin
val repository = GeocodingCacheRepositoryImpl(database)
val cache = DatabaseGeocodingCache(repository)

// Get cached result within 100m radius
val cached = cache.get(latitude = 41.7151, longitude = 44.8271)

// Cache a new result (30-day TTL)
cache.put(geocodedLocation)
```

**Spatial Indexing:**
- Bounding box queries for efficiency
- Haversine distance calculation for accuracy
- Configurable proximity threshold (default 100m)

### Location Sample Storage

```kotlin
val repository = LocationRepositoryImpl(database)

// Insert sample
repository.insertSample(locationSample)

// Query by time range
val samples = repository.getSamples(
    userId = "user_id",
    startTime = startInstant,
    endTime = endInstant
)

// Get unprocessed samples
val unprocessed = repository.getUnprocessedSamples(userId, limit = 1000)
```

**Features:**
- Soft delete support
- Time-based queries with indices
- Trip assignment tracking
- Bulk operations

### Place Visit Storage

```kotlin
val repository = PlaceVisitRepositoryImpl(database)

// Insert visit with linked samples
repository.insertVisit(placeVisit)

// Query by time range
val visits = repository.getVisits(userId, startTime, endTime)

// Link additional samples
repository.linkSamples(visitId, sampleIds)
```

**Features:**
- Many-to-many sample linkage
- Geocoded address storage
- Time range queries
- Pagination support

## Usage

### Android Setup

```kotlin
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.db.DatabaseDriverFactory

// In your Application or DI module
val driverFactory = DatabaseDriverFactory(context)
val database = Database(driverFactory)

// Create repositories
val locationRepo = LocationRepositoryImpl(database)
val placeVisitRepo = PlaceVisitRepositoryImpl(database)
val geocodingRepo = GeocodingCacheRepositoryImpl(database)
```

### iOS Setup

```kotlin
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.db.DatabaseDriverFactory

// In your app initialization
val driverFactory = DatabaseDriverFactory()
val database = Database(driverFactory)

// Create repositories
val locationRepo = LocationRepositoryImpl(database)
val placeVisitRepo = PlaceVisitRepositoryImpl(database)
val geocodingRepo = GeocodingCacheRepositoryImpl(database)
```

### Complete Integration Example

```kotlin
// Set up database and repositories
val database = Database(DatabaseDriverFactory(context)) // Android
val locationRepo = LocationRepositoryImpl(database)
val placeVisitRepo = PlaceVisitRepositoryImpl(database)
val geocodingRepo = GeocodingCacheRepositoryImpl(database)

// Set up geocoding with database cache
val platformGeocoder = createAndroidReverseGeocoder(context) // Android
val cache = DatabaseGeocodingCache(geocodingRepo)
val cachedGeocoder = DatabaseCachedReverseGeocoder(platformGeocoder, cache)

// Create location service
val locationService = DatabaseLocationService(
    locationRepository = locationRepo,
    placeVisitRepository = placeVisitRepo,
    reverseGeocoder = cachedGeocoder,
    userId = currentUserId
)

// Record location sample
val sample = LocationSample(
    id = UUID.randomUUID().toString(),
    timestamp = Clock.System.now(),
    latitude = 41.7151,
    longitude = 44.8271,
    accuracy = 10.0,
    source = LocationSource.GPS,
    deviceId = deviceId,
    userId = userId
)

locationService.recordSample(sample)

// Process samples to detect place visits (with geocoding)
locationService.processSamples()

// Query results
val recentVisits = locationService.getPlaceVisits(limit = 20)
recentVisits.forEach { visit ->
    println("${visit.city}, ${visit.countryCode}")
    println("At: ${visit.poiName ?: visit.approximateAddress}")
}
```

## Database Schema

### location_samples

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT | Primary key |
| timestamp | INTEGER | Unix timestamp in milliseconds |
| latitude | REAL | Latitude coordinate |
| longitude | REAL | Longitude coordinate |
| accuracy | REAL | Accuracy in meters |
| speed | REAL | Speed in m/s (nullable) |
| bearing | REAL | Bearing in degrees (nullable) |
| source | TEXT | GPS/NETWORK/VISIT/SIGNIFICANT_CHANGE |
| trip_id | TEXT | Associated trip (nullable) |
| user_id | TEXT | User identifier |
| deleted_at | INTEGER | Soft delete timestamp (nullable) |

**Indices:**
- `timestamp` - For time-based queries
- `user_id, trip_id` - For trip queries
- `user_id, timestamp DESC` - For recent samples
- `deleted_at` - For filtering deleted records

### place_visits

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT | Primary key |
| start_time | INTEGER | Visit start timestamp |
| end_time | INTEGER | Visit end timestamp |
| center_latitude | REAL | Center point latitude |
| center_longitude | REAL | Center point longitude |
| approximate_address | TEXT | Full address (nullable) |
| poi_name | TEXT | Point of interest name (nullable) |
| city | TEXT | City name (nullable) |
| country_code | TEXT | ISO country code (nullable) |
| user_id | TEXT | User identifier |
| deleted_at | INTEGER | Soft delete timestamp (nullable) |

**Indices:**
- `start_time, end_time` - For time range queries
- `user_id` - For user queries
- `user_id, start_time DESC` - For recent visits

### place_visit_samples (junction table)

| Column | Type | Description |
|--------|------|-------------|
| place_visit_id | TEXT | Foreign key to place_visits |
| location_sample_id | TEXT | Foreign key to location_samples |

**Primary Key:** `(place_visit_id, location_sample_id)`

### geocoding_cache

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT | Primary key (lat,lon) |
| latitude | REAL | Latitude coordinate |
| longitude | REAL | Longitude coordinate |
| formatted_address | TEXT | Full formatted address |
| city | TEXT | City name |
| state | TEXT | State/province |
| country_code | TEXT | ISO country code |
| country_name | TEXT | Country full name |
| postal_code | TEXT | Postal/ZIP code |
| poi_name | TEXT | POI name |
| street | TEXT | Street name |
| street_number | TEXT | Street number |
| cached_at | INTEGER | Cache timestamp |
| expires_at | INTEGER | Expiration timestamp |

**Indices:**
- `latitude, longitude` - For spatial queries
- `expires_at` - For cleanup queries

## Performance Considerations

**Indexing Strategy:**
- All time-based queries use indexed columns
- User-scoped queries use composite indices
- Spatial queries use bounding box + Haversine

**Batch Operations:**
- Process up to 1000 samples at a time
- Use transactions for multi-record inserts
- Bulk sample linking via junction table

**Cache Management:**
- Automatic expiration (30-day default)
- Spatial proximity matching (100m default)
- Periodic cleanup of expired entries

## Future Enhancements

Planned improvements:
- [ ] Database migrations support
- [ ] Backup and restore functionality
- [ ] Query result caching (Flow-based)
- [ ] Full-text search for addresses
- [ ] Spatial indices (R-tree) for better performance
- [ ] Compression for old location samples
- [ ] Multi-user support with proper isolation

## See Also

- [IMPLEMENTATION_NEXT_STEPS.md](../../../IMPLEMENTATION_NEXT_STEPS.md) - Phase 1A details
- [location/README.md](../location/README.md) - Geocoding infrastructure
- [TODO.md](../../../TODO.md) - Database-related tasks
