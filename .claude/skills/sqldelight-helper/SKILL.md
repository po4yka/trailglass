---
name: sqldelight-helper
description: Assists with SQLDelight schema design, query optimization, and migration generation for the Trailglass database. Use when working with database schemas, queries, or migrations.
allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob
  - Bash
---

# SQLDelight Helper

Expert assistance for SQLDelight database operations, schema design, query optimization, and migrations in Trailglass.

## Database Schema

### Current Tables

**location_samples** - Raw GPS points
```sql
CREATE TABLE location_samples (
    id TEXT PRIMARY KEY NOT NULL,
    timestamp INTEGER NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    accuracy REAL NOT NULL,
    speed REAL,
    bearing REAL,
    source TEXT NOT NULL,
    trip_id TEXT,
    user_id TEXT NOT NULL,
    device_id TEXT NOT NULL,
    deleted_at INTEGER,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL
);

CREATE INDEX idx_location_samples_timestamp ON location_samples(timestamp);
CREATE INDEX idx_location_samples_user_trip ON location_samples(user_id, trip_id);
CREATE INDEX idx_location_samples_user_time ON location_samples(user_id, timestamp DESC);
CREATE INDEX idx_location_samples_deleted ON location_samples(deleted_at);
```

**place_visits** - Detected stationary periods
```sql
CREATE TABLE place_visits (
    id TEXT PRIMARY KEY NOT NULL,
    trip_id TEXT,
    user_id TEXT NOT NULL,
    start_time INTEGER NOT NULL,
    end_time INTEGER NOT NULL,
    center_latitude REAL NOT NULL,
    center_longitude REAL NOT NULL,
    radius_meters REAL NOT NULL,
    approximate_address TEXT,
    poi_name TEXT,
    city TEXT,
    country TEXT,
    country_code TEXT,
    confidence REAL NOT NULL,
    arrival_transport_type TEXT,
    departure_transport_type TEXT,
    user_notes TEXT,
    deleted_at INTEGER,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

CREATE INDEX idx_place_visits_time ON place_visits(start_time, end_time);
CREATE INDEX idx_place_visits_user ON place_visits(user_id);
CREATE INDEX idx_place_visits_user_time ON place_visits(user_id, start_time DESC);
```

**route_segments** - Movement between places
**trips** - Multi-day journeys
**photos** - Photo metadata
**photo_attachments** - Photo-visit links
**geocoding_cache** - Persistent reverse geocoding cache

## Query Writing

### Best Practices

**1. Named Queries with Clear Purpose**
```sql
-- ✅ Good: Clear what it does
getById:
SELECT * FROM place_visits
WHERE id = ? AND user_id = ?;

getForTimeRange:
SELECT * FROM place_visits
WHERE user_id = ?
  AND start_time >= ?
  AND end_time <= ?
  AND deleted_at IS NULL
ORDER BY start_time ASC;

-- ❌ Bad: Unclear purpose
query1:
SELECT * FROM place_visits WHERE id = ?;
```

**2. Parameterized Queries**
```sql
-- ✅ Good: Uses parameters
getByCity:
SELECT * FROM place_visits
WHERE user_id = ? AND city = ?;

-- ❌ Bad: Never concatenate in application
-- Will SQL inject if you build string in Kotlin
```

**3. Specific Column Selection**
```sql
-- ✅ Good: Select only needed columns
getBasicInfo:
SELECT id, city, country_code, start_time, end_time
FROM place_visits
WHERE user_id = ?;

-- ⚠️ OK but less efficient: SELECT *
-- Only use SELECT * when you need all columns
```

**4. Proper Indexing**
```sql
-- ✅ Good: Index on queried columns
CREATE INDEX idx_place_visits_user_time
ON place_visits(user_id, start_time DESC);

-- Then queries on user_id and start_time are fast
SELECT * FROM place_visits
WHERE user_id = ? AND start_time >= ?
ORDER BY start_time DESC;
```

**5. Soft Deletes**
```sql
-- ✅ Good: Soft delete
softDelete:
UPDATE place_visits
SET deleted_at = ?
WHERE id = ? AND user_id = ?;

-- Get active records
getActive:
SELECT * FROM place_visits
WHERE user_id = ? AND deleted_at IS NULL;

-- ❌ Bad: Hard delete (loses data)
DELETE FROM place_visits WHERE id = ?;
```

**6. Batch Operations**
```sql
-- Insert query
insert:
INSERT INTO location_samples(
    id, timestamp, latitude, longitude,
    accuracy, source, user_id, device_id
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);

-- Use in transaction for batch
```kotlin
database.transaction {
    samples.forEach { sample ->
        database.locationSamplesQueries.insert(
            id = sample.id,
            timestamp = sample.timestamp.toEpochMilliseconds(),
            latitude = sample.latitude,
            longitude = sample.longitude,
            accuracy = sample.accuracy,
            source = sample.source.name,
            userId = sample.userId,
            deviceId = sample.deviceId
        )
    }
}
```

### Common Query Patterns

**Time Range Queries:**
```sql
getForTimeRange:
SELECT * FROM place_visits
WHERE user_id = :userId
  AND start_time >= :startTime
  AND end_time <= :endTime
  AND deleted_at IS NULL
ORDER BY start_time ASC;
```

**Pagination:**
```sql
getPaginated:
SELECT * FROM place_visits
WHERE user_id = ?
  AND deleted_at IS NULL
ORDER BY start_time DESC
LIMIT ? OFFSET ?;
```

**Aggregation:**
```sql
countByCountry:
SELECT country_code, COUNT(*) as visit_count
FROM place_visits
WHERE user_id = ? AND deleted_at IS NULL
GROUP BY country_code
ORDER BY visit_count DESC;
```

**Join Queries:**
```sql
getVisitWithSamples:
SELECT
    pv.*,
    ls.id as sample_id,
    ls.latitude as sample_lat,
    ls.longitude as sample_lon
FROM place_visits pv
JOIN place_visit_samples pvs ON pv.id = pvs.place_visit_id
JOIN location_samples ls ON pvs.location_sample_id = ls.id
WHERE pv.id = ?;
```

**Spatial Queries (Geocoding Cache):**
```sql
-- Bounding box + Haversine distance
getNearby:
SELECT *
FROM geocoding_cache
WHERE latitude BETWEEN :minLat AND :maxLat
  AND longitude BETWEEN :minLon AND :maxLon
  AND expires_at > :currentTime;

-- Calculate bounding box in Kotlin:
// ±100m ≈ ±0.001 degrees
```

## Schema Design

### Design Principles

**1. Normalization**
- Avoid data duplication
- Use foreign keys for relationships
- Separate concerns (location samples vs visits)

**2. Indexing Strategy**
- Index columns in WHERE clauses
- Index columns in ORDER BY
- Index foreign keys
- Composite indices for common queries

**3. Data Types**
```sql
-- Use appropriate types
id TEXT                  -- UUIDs
timestamp INTEGER        -- Unix milliseconds (Instant)
latitude REAL           -- Floating point
longitude REAL
is_active INTEGER       -- Boolean (0/1)
```

**4. Constraints**
```sql
-- NOT NULL for required fields
user_id TEXT NOT NULL

-- Foreign keys with cascade
FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE

-- Check constraints for validation
CHECK (latitude >= -90 AND latitude <= 90)
CHECK (longitude >= -180 AND longitude <= 180)
```

### Adding New Table

**Process:**
1. Define schema in new `.sq` file
2. Add indices for queries
3. Regenerate SQLDelight code
4. Create repository interface
5. Implement repository
6. Add DI bindings
7. Write tests

**Example:**
```sql
-- shared/src/commonMain/sqldelight/.../JournalEntries.sq

CREATE TABLE journal_entries (
    id TEXT PRIMARY KEY NOT NULL,
    place_visit_id TEXT,
    user_id TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    deleted_at INTEGER,
    FOREIGN KEY (place_visit_id) REFERENCES place_visits(id) ON DELETE CASCADE
);

CREATE INDEX idx_journal_entries_visit ON journal_entries(place_visit_id);
CREATE INDEX idx_journal_entries_user ON journal_entries(user_id);

-- Queries
insert:
INSERT INTO journal_entries(id, place_visit_id, user_id, content, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?);

getById:
SELECT * FROM journal_entries
WHERE id = ? AND user_id = ? AND deleted_at IS NULL;

getForVisit:
SELECT * FROM journal_entries
WHERE place_visit_id = ? AND deleted_at IS NULL
ORDER BY created_at DESC;

update:
UPDATE journal_entries
SET content = ?, updated_at = ?
WHERE id = ? AND user_id = ?;

softDelete:
UPDATE journal_entries
SET deleted_at = ?
WHERE id = ? AND user_id = ?;
```

## Migrations

### Migration Strategy

**Version Control:**
- Track schema version
- Create migration files
- Test migrations thoroughly

**Migration Files:**
```
shared/src/commonMain/sqldelight/migrations/
├── 1.sqm  # Initial schema (baseline)
├── 2.sqm  # First migration
├── 3.sqm  # Second migration
```

**Migration Format:**
```sql
-- 2.sqm: Add user_notes to place_visits

ALTER TABLE place_visits
ADD COLUMN user_notes TEXT;

-- Optional: Add index
CREATE INDEX idx_place_visits_notes ON place_visits(user_notes);
```

### Migration Examples

**Add Column:**
```sql
-- 3.sqm
ALTER TABLE place_visits
ADD COLUMN weather TEXT;
```

**Add Table:**
```sql
-- 4.sqm
CREATE TABLE weather_data (
    id TEXT PRIMARY KEY NOT NULL,
    place_visit_id TEXT NOT NULL,
    temperature REAL,
    conditions TEXT,
    FOREIGN KEY (place_visit_id) REFERENCES place_visits(id) ON DELETE CASCADE
);

CREATE INDEX idx_weather_visit ON weather_data(place_visit_id);
```

**Add Index:**
```sql
-- 5.sqm
CREATE INDEX idx_location_samples_source ON location_samples(source);
```

**Data Migration:**
```sql
-- 6.sqm: Populate new column from existing data
UPDATE place_visits
SET country_code = (
    CASE country
        WHEN 'France' THEN 'FR'
        WHEN 'Germany' THEN 'DE'
        WHEN 'United States' THEN 'US'
        ELSE NULL
    END
)
WHERE country_code IS NULL;
```

### Applying Migrations

```kotlin
// In database setup
fun createDatabase(driver: SqlDriver): TrailGlassDatabase {
    val currentVersion = 6  // Update when adding migrations
    val oldVersion = // ... get from preferences

    if (oldVersion < currentVersion) {
        TrailGlassDatabase.Schema.migrate(
            driver = driver,
            oldVersion = oldVersion,
            newVersion = currentVersion
        )
        // Save new version to preferences
    }

    return TrailGlassDatabase(driver)
}
```

## Query Optimization

### Performance Issues

**Issue: Slow Query**
```sql
-- Slow: No index on city
SELECT * FROM place_visits
WHERE city = ?;
```

**Solution: Add Index**
```sql
CREATE INDEX idx_place_visits_city ON place_visits(city);
```

**Issue: SELECT ***
```sql
-- Slow: Fetches all columns
SELECT * FROM place_visits WHERE user_id = ?;
```

**Solution: Select Specific Columns**
```sql
-- Fast: Only needed columns
SELECT id, city, start_time, end_time
FROM place_visits WHERE user_id = ?;
```

**Issue: Multiple Queries**
```kotlin
// Slow: N+1 queries
val visits = database.placeVisitsQueries.getAll().executeAsList()
visits.forEach { visit ->
    val samples = database.locationSamplesQueries.getForVisit(visit.id)
    // ...
}
```

**Solution: Single Query with JOIN**
```sql
-- Fast: Single query
getVisitsWithSampleCount:
SELECT
    pv.*,
    COUNT(pvs.location_sample_id) as sample_count
FROM place_visits pv
LEFT JOIN place_visit_samples pvs ON pv.id = pvs.place_visit_id
WHERE pv.user_id = ?
GROUP BY pv.id;
```

### Query Analysis

**Check Query Plan:**
```sql
EXPLAIN QUERY PLAN
SELECT * FROM place_visits
WHERE user_id = ? AND start_time >= ?
ORDER BY start_time DESC;

-- Look for "USING INDEX" (good) vs "SCAN TABLE" (bad)
```

## Code Generation

### After Schema Changes

```bash
# Regenerate SQLDelight code
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface

# Clean build
./gradlew clean build

# Verify compilation
./gradlew :shared:compileKotlinMetadata
```

### Generated Code Location

```
shared/build/generated/sqldelight/code/TrailGlassDatabase/
├── commonMain/
│   └── com/po4yka/trailglass/db/
│       ├── TrailGlassDatabase.kt
│       ├── PlaceVisitsQueries.kt
│       └── ...
```

## Repository Implementation

### Mapping DB to Domain

```kotlin
// Extension function for mapping
fun PlaceVisitEntity.toDomain(): PlaceVisit {
    return PlaceVisit(
        id = id,
        tripId = trip_id,
        userId = user_id,
        startTime = Instant.fromEpochMilliseconds(start_time),
        endTime = Instant.fromEpochMilliseconds(end_time),
        centerLatitude = center_latitude,
        centerLongitude = center_longitude,
        // ... map all fields
    )
}

// In repository
override suspend fun getPlaceVisitById(
    id: String,
    userId: String
): PlaceVisit? {
    return database.placeVisitsQueries
        .getById(id, userId)
        .executeAsOneOrNull()
        ?.toDomain()
}
```

### Flow-Based Queries

```kotlin
override fun getPlaceVisitsForTimeRange(
    userId: String,
    startTime: Instant,
    endTime: Instant
): Flow<List<PlaceVisit>> {
    return database.placeVisitsQueries
        .getForTimeRange(
            userId = userId,
            startTime = startTime.toEpochMilliseconds(),
            endTime = endTime.toEpochMilliseconds()
        )
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { it.map { entity -> entity.toDomain() } }
}
```

## Testing

### Repository Tests

```kotlin
@Test
fun testInsertAndRetrieve() = runTest {
    // Arrange
    val visit = createTestVisit()

    // Act
    repository.insertPlaceVisit(visit)
    val result = repository.getPlaceVisitById(visit.id, userId)

    // Assert
    assertNotNull(result)
    assertEquals(visit.id, result.id)
}

@Test
fun testQueryWithIndices() = runTest {
    // Insert large dataset
    repeat(1000) { i ->
        repository.insertPlaceVisit(createTestVisit(id = "v$i"))
    }

    // Query should be fast due to indices
    val start = Clock.System.now()
    val results = repository.getForTimeRange(userId, start, end).first()
    val duration = Clock.System.now() - start

    // Should complete quickly
    assertTrue(duration < 1.seconds)
}
```

## Troubleshooting

### "Cannot resolve symbol"
```bash
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface
# Then: File → Invalidate Caches → Restart in IDE
```

### "Syntax error in SQL"
- Check SQLite syntax (not MySQL/PostgreSQL)
- Verify table/column names exist
- Check parameter placeholders (?)

### "Foreign key constraint failed"
- Ensure referenced row exists
- Check CASCADE behavior
- Verify order of operations

## Related Documentation

- `.cursor/rules/sqldelight.mdc` - SQLDelight rules
- `shared/src/commonMain/kotlin/data/README.md` - Data layer docs
- SQLDelight docs: https://cashapp.github.io/sqldelight/

---

*Use this skill for SQLDelight schema design, query optimization, and migration management in Trailglass.*
