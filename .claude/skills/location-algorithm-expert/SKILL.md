---
name: location-algorithm-expert
description: Expert guidance on location processing algorithms including DBSCAN clustering, trip detection, route classification, and geocoding strategies. Use when implementing or optimizing location analysis features.
allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob
---

# Location Algorithm Expert

Specialized guidance for implementing location processing algorithms in the Trailglass travel logging system.

## Location Processing Pipeline

```
Raw Location Samples
    ↓
Clustering (DBSCAN)
    ↓
Place Visit Detection
    ↓
Reverse Geocoding (with cache)
    ↓
Route Segment Generation
    ↓
Transport Type Classification
    ↓
Trip Assembly
    ↓
Day Aggregation
```

## DBSCAN Clustering

### Algorithm Overview

DBSCAN (Density-Based Spatial Clustering of Applications with Noise) groups location points that are close together in space and time.

**Key Concepts:**
- **Core Point**: Has ≥ minPoints within epsilon radius
- **Border Point**: Within epsilon of core point but has < minPoints neighbors
- **Noise**: Points that don't belong to any cluster

**Parameters:**
- **epsilon (ε)**: Maximum distance between points (default: 100 meters)
- **minPoints**: Minimum points to form cluster (default: 3)
- **timeWindow**: Maximum time gap between points (default: 30 minutes)

### Implementation

```kotlin
class PlaceVisitDetector(
    private val epsilon: Double = 100.0,  // meters
    private val minPoints: Int = 3,
    private val timeWindow: Duration = 30.minutes
) {
    suspend fun detectPlaceVisits(
        samples: List<LocationSample>
    ): List<PlaceVisit> {
        // 1. Sort by timestamp
        val sorted = samples.sortedBy { it.timestamp }

        // 2. Build spatial index (for performance)
        val spatialIndex = buildKDTree(sorted)

        // 3. Cluster using DBSCAN
        val clusters = dbscan(sorted, spatialIndex)

        // 4. Filter by duration and size
        val significantClusters = clusters.filter { cluster ->
            val duration = cluster.endTime - cluster.startTime
            duration >= minStayDuration &&
            cluster.samples.size >= minPoints
        }

        // 5. Convert clusters to place visits
        return significantClusters.map { cluster ->
            createPlaceVisit(cluster)
        }
    }

    private fun dbscan(
        samples: List<LocationSample>,
        spatialIndex: KDTree
    ): List<Cluster> {
        val visited = mutableSetOf<String>()
        val clusters = mutableListOf<Cluster>()

        for (sample in samples) {
            if (sample.id in visited) continue

            // Find neighbors
            val neighbors = findNeighbors(sample, samples, spatialIndex)

            if (neighbors.size < minPoints) {
                // Noise point
                continue
            }

            // Create new cluster
            val cluster = expandCluster(
                sample = sample,
                neighbors = neighbors,
                samples = samples,
                spatialIndex = spatialIndex,
                visited = visited
            )

            clusters.add(cluster)
        }

        return clusters
    }

    private fun findNeighbors(
        sample: LocationSample,
        samples: List<LocationSample>,
        spatialIndex: KDTree
    ): List<LocationSample> {
        return spatialIndex.searchRadius(
            lat = sample.latitude,
            lon = sample.longitude,
            radius = epsilon
        ).filter { neighbor ->
            // Also check time constraint
            val timeDiff = abs(sample.timestamp - neighbor.timestamp)
            timeDiff <= timeWindow
        }
    }

    private fun expandCluster(
        sample: LocationSample,
        neighbors: List<LocationSample>,
        samples: List<LocationSample>,
        spatialIndex: KDTree,
        visited: MutableSet<String>
    ): Cluster {
        val clusterSamples = mutableListOf(sample)
        visited.add(sample.id)

        val queue = ArrayDeque(neighbors)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            if (current.id in visited) continue
            visited.add(current.id)

            clusterSamples.add(current)

            // Find neighbors of current point
            val currentNeighbors = findNeighbors(current, samples, spatialIndex)

            if (currentNeighbors.size >= minPoints) {
                // Core point - add its neighbors to queue
                queue.addAll(currentNeighbors)
            }
        }

        return Cluster(
            samples = clusterSamples,
            centerLat = clusterSamples.map { it.latitude }.average(),
            centerLon = clusterSamples.map { it.longitude }.average(),
            startTime = clusterSamples.minOf { it.timestamp },
            endTime = clusterSamples.maxOf { it.timestamp }
        )
    }
}
```

### Optimization Tips

**1. Spatial Indexing:**
```kotlin
// Use K-D Tree for fast nearest neighbor search
// O(log n) vs O(n) for naive search
class KDTree(samples: List<LocationSample>) {
    fun searchRadius(lat: Double, lon: Double, radius: Double): List<LocationSample>
}
```

**2. Early Termination:**
```kotlin
// Skip if distance > epsilon + accuracy
if (haversineDistance(p1, p2) > epsilon + p1.accuracy + p2.accuracy) {
    continue
}
```

**3. Time Pre-filtering:**
```kotlin
// Filter by time before distance calculation
val candidateSamples = samples.filter { neighbor ->
    abs(sample.timestamp - neighbor.timestamp) <= timeWindow
}
```

### Parameter Tuning

**Epsilon (Spatial Tolerance):**
- Too small: Fragments single visits into multiple clusters
- Too large: Merges nearby distinct visits
- Recommended: 100m for urban, 200m for suburban

**MinPoints:**
- Too small: Noise becomes visits
- Too large: Misses short visits
- Recommended: 3-5 points

**Time Window:**
- Too small: Breaks continuous visits
- Too large: Merges separate visits
- Recommended: 30 minutes

## Place Visit Creation

### Center Point Calculation

```kotlin
fun calculateCenter(samples: List<LocationSample>): Pair<Double, Double> {
    // Weighted average by accuracy (higher accuracy = more weight)
    val totalWeight = samples.sumOf { 1.0 / it.accuracy }

    val centerLat = samples.sumOf {
        (it.latitude / it.accuracy) / totalWeight
    }

    val centerLon = samples.sumOf {
        (it.longitude / it.accuracy) / totalWeight
    }

    return Pair(centerLat, centerLon)
}
```

### Radius Calculation

```kotlin
fun calculateRadius(
    samples: List<LocationSample>,
    centerLat: Double,
    centerLon: Double
): Double {
    return samples.maxOf { sample ->
        haversineDistance(
            lat1 = centerLat,
            lon1 = centerLon,
            lat2 = sample.latitude,
            lon2 = sample.longitude
        )
    }
}
```

### Confidence Score

```kotlin
fun calculateConfidence(
    samples: List<LocationSample>,
    radius: Double
): Double {
    // Factors:
    // 1. Number of samples (more = higher confidence)
    val sampleScore = min(samples.size / 10.0, 1.0)

    // 2. Average accuracy (better accuracy = higher confidence)
    val avgAccuracy = samples.map { it.accuracy }.average()
    val accuracyScore = max(0.0, 1.0 - (avgAccuracy / 100.0))

    // 3. Cluster tightness (smaller radius = higher confidence)
    val tightnessScore = max(0.0, 1.0 - (radius / 200.0))

    // Weighted combination
    return (sampleScore * 0.4 + accuracyScore * 0.3 + tightnessScore * 0.3)
        .coerceIn(0.0, 1.0)
}
```

## Reverse Geocoding

### Caching Strategy

```kotlin
class CachedReverseGeocoder(
    private val geocoder: ReverseGeocoder,
    private val cache: GeocodingCache
) {
    suspend fun geocode(
        latitude: Double,
        longitude: Double
    ): GeocodedLocation? {
        // 1. Check cache (within 100m)
        val cached = cache.findNearby(
            lat = latitude,
            lon = longitude,
            radius = 100.0
        )

        if (cached != null && !cached.isExpired()) {
            return cached
        }

        // 2. Call platform geocoder
        val result = try {
            geocoder.reverseGeocode(latitude, longitude)
        } catch (e: Exception) {
            logger.warn { "Geocoding failed: ${e.message}" }
            return null
        }

        // 3. Store in cache (30-day TTL)
        cache.store(result, ttl = 30.days)

        return result
    }
}
```

### Spatial Cache Query

```kotlin
suspend fun findNearby(
    lat: Double,
    lon: Double,
    radius: Double
): GeocodedLocation? {
    // Bounding box for fast filtering
    val latDelta = radius / 111320.0  // meters to degrees
    val lonDelta = radius / (111320.0 * cos(lat * PI / 180))

    val candidates = database.geocodingCacheQueries
        .getNearby(
            minLat = lat - latDelta,
            maxLat = lat + latDelta,
            minLon = lon - lonDelta,
            maxLon = lon + lonDelta
        )
        .executeAsList()

    // Precise distance check
    return candidates
        .map { it to haversineDistance(lat, lon, it.latitude, it.longitude) }
        .filter { (_, distance) -> distance <= radius }
        .minByOrNull { (_, distance) -> distance }
        ?.first
        ?.toDomain()
}
```

## Route Segment Generation

### Route Detection

```kotlin
fun generateRouteSegments(
    visits: List<PlaceVisit>
): List<RouteSegment> {
    if (visits.size < 2) return emptyList()

    return visits
        .sortedBy { it.startTime }
        .zipWithNext { from, to ->
            // Gap between visits
            val gapStart = from.endTime
            val gapEnd = to.startTime

            // Get samples in gap
            val routeSamples = getSamplesBetween(gapStart, gapEnd)

            // Create route segment
            RouteSegment(
                id = UUID.randomUUID().toString(),
                startTime = gapStart,
                endTime = gapEnd,
                startLatitude = from.centerLatitude,
                startLongitude = from.centerLongitude,
                endLatitude = to.centerLatitude,
                endLongitude = to.centerLongitude,
                transportType = classifyTransportType(routeSamples),
                distanceMeters = calculateRouteDistance(routeSamples),
                pathPoints = simplifyPath(routeSamples)
            )
        }
}
```

### Transport Type Classification

```kotlin
fun classifyTransportType(samples: List<LocationSample>): TransportType {
    if (samples.isEmpty()) return TransportType.UNKNOWN

    // Calculate average and max speed
    val speeds = samples.mapNotNull { it.speed }
    if (speeds.isEmpty()) return TransportType.UNKNOWN

    val avgSpeed = speeds.average()  // m/s
    val maxSpeed = speeds.maxOrNull() ?: 0.0

    // Calculate total distance
    val distance = samples
        .zipWithNext { a, b ->
            haversineDistance(a.latitude, a.longitude, b.latitude, b.longitude)
        }
        .sum()

    // Speed in km/h
    val avgSpeedKmh = avgSpeed * 3.6
    val maxSpeedKmh = maxSpeed * 3.6

    return when {
        // Walking: slow speed, any distance
        avgSpeedKmh < 7 -> TransportType.WALK

        // Cycling: moderate speed
        avgSpeedKmh < 25 -> TransportType.BICYCLE

        // Car: higher speed, shorter/medium distance
        maxSpeedKmh < 120 && distance < 500_000 -> TransportType.CAR

        // Train: high speed, medium/long distance, less variation
        avgSpeedKmh > 60 && distance > 50_000 -> {
            val speedVariation = speeds.standardDeviation() / avgSpeed
            if (speedVariation < 0.5) TransportType.TRAIN else TransportType.CAR
        }

        // Plane: very high speed or very long distance
        maxSpeedKmh > 200 || distance > 500_000 -> TransportType.FLIGHT

        // Default
        else -> TransportType.CAR
    }
}
```

### Path Simplification (Douglas-Peucker)

```kotlin
fun simplifyPath(
    samples: List<LocationSample>,
    epsilon: Double = 50.0  // meters
): List<LatLon> {
    if (samples.size <= 2) {
        return samples.map { LatLon(it.latitude, it.longitude) }
    }

    val points = samples.map { LatLon(it.latitude, it.longitude) }
    return douglasPeucker(points, epsilon)
}

fun douglasPeucker(
    points: List<LatLon>,
    epsilon: Double
): List<LatLon> {
    if (points.size <= 2) return points

    // Find point with max distance from line
    val first = points.first()
    val last = points.last()

    var maxDistance = 0.0
    var maxIndex = 0

    for (i in 1 until points.size - 1) {
        val distance = perpendicularDistance(points[i], first, last)
        if (distance > maxDistance) {
            maxDistance = distance
            maxIndex = i
        }
    }

    return if (maxDistance > epsilon) {
        // Recursively simplify segments
        val left = douglasPeucker(points.subList(0, maxIndex + 1), epsilon)
        val right = douglasPeucker(points.subList(maxIndex, points.size), epsilon)
        left.dropLast(1) + right
    } else {
        // Remove all points between first and last
        listOf(first, last)
    }
}

fun perpendicularDistance(
    point: LatLon,
    lineStart: LatLon,
    lineEnd: LatLon
): Double {
    // Perpendicular distance from point to line segment
    // Using Haversine for accuracy
    val A = point.latitude - lineStart.latitude
    val B = point.longitude - lineStart.longitude
    val C = lineEnd.latitude - lineStart.latitude
    val D = lineEnd.longitude - lineStart.longitude

    val dot = A * C + B * D
    val lenSq = C * C + D * D
    val param = if (lenSq != 0.0) dot / lenSq else -1.0

    val xx: Double
    val yy: Double

    when {
        param < 0 -> {
            xx = lineStart.latitude
            yy = lineStart.longitude
        }
        param > 1 -> {
            xx = lineEnd.latitude
            yy = lineEnd.longitude
        }
        else -> {
            xx = lineStart.latitude + param * C
            yy = lineStart.longitude + param * D
        }
    }

    return haversineDistance(point.latitude, point.longitude, xx, yy)
}
```

## Trip Detection

### Trip Assembly Logic

```kotlin
fun detectTrips(visits: List<PlaceVisit>): List<Trip> {
    if (visits.isEmpty()) return emptyList()

    val sorted = visits.sortedBy { it.startTime }
    val trips = mutableListOf<Trip>()
    var currentTrip = mutableListOf(sorted.first())

    for (i in 1 until sorted.size) {
        val prevVisit = sorted[i - 1]
        val currVisit = sorted[i]

        // Calculate gap
        val gap = currVisit.startTime - prevVisit.endTime

        // Check if new trip
        val isNewTrip = when {
            // Long time gap (>24 hours)
            gap > 24.hours -> true

            // Return home (similar location to first visit)
            isNearby(currVisit, sorted.first(), radius = 5000.0) -> {
                // Returned home - end trip
                true
            }

            // Same city for extended period (>7 days)
            currVisit.city == prevVisit.city &&
            (currVisit.startTime - currentTrip.first().startTime) > 7.days -> true

            else -> false
        }

        if (isNewTrip) {
            // Save current trip
            trips.add(createTrip(currentTrip))

            // Start new trip
            currentTrip = mutableListOf(currVisit)
        } else {
            // Continue current trip
            currentTrip.add(currVisit)
        }
    }

    // Don't forget last trip
    if (currentTrip.isNotEmpty()) {
        trips.add(createTrip(currentTrip))
    }

    return trips
}

fun createTrip(visits: List<PlaceVisit>): Trip {
    return Trip(
        id = UUID.randomUUID().toString(),
        userId = visits.first().userId,
        startTime = visits.first().startTime,
        endTime = visits.last().endTime,
        visitIds = visits.map { it.id },
        mainDestination = determineMainDestination(visits),
        countries = visits.mapNotNull { it.countryCode }.distinct(),
        cities = visits.mapNotNull { it.city }.distinct(),
        totalDistance = calculateTripDistance(visits)
    )
}

fun determineMainDestination(visits: List<PlaceVisit>): String? {
    // Destination where most time was spent
    return visits
        .groupBy { it.city }
        .mapValues { (_, cityVisits) ->
            cityVisits.sumOf {
                (it.endTime - it.startTime).inWholeSeconds
            }
        }
        .maxByOrNull { it.value }
        ?.key
}
```

## Utilities

### Haversine Distance

```kotlin
fun haversineDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val R = 6371000.0  // Earth radius in meters

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c  // Distance in meters
}
```

### Bearing Calculation

```kotlin
fun calculateBearing(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)

    val y = sin(dLon) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) -
            sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

    val bearing = Math.toDegrees(atan2(y, x))
    return (bearing + 360) % 360  // Normalize to 0-360
}
```

## Performance Considerations

**1. Batch Processing:**
```kotlin
// Process in chunks to avoid memory issues
suspend fun processSamplesInBatches(
    samples: List<LocationSample>,
    batchSize: Int = 1000
) {
    samples.chunked(batchSize).forEach { batch ->
        val visits = detectPlaceVisits(batch)
        saveVisits(visits)
    }
}
```

**2. Incremental Processing:**
```kotlin
// Only process new samples
suspend fun processNewSamples(userId: String) {
    val lastProcessed = getLastProcessedTime(userId)
    val newSamples = getSamplesSince(userId, lastProcessed)

    if (newSamples.size >= minSamplesForProcessing) {
        val visits = detectPlaceVisits(newSamples)
        saveVisits(visits)
        updateLastProcessedTime(userId, Clock.System.now())
    }
}
```

**3. Background Processing:**
```kotlin
// Use WorkManager (Android) or Background Tasks (iOS)
// Don't block UI thread
```

## Testing Algorithms

```kotlin
@Test
fun testDBSCAN_clustersNearbyPoints() = runTest {
    // Arrange: Create cluster of points in Paris
    val parisPoints = List(10) { i ->
        createSample(
            lat = 48.8566 + (Random.nextDouble() * 0.001),  // Within 100m
            lon = 2.3522 + (Random.nextDouble() * 0.001),
            timestamp = baseTime + (i * 1.minutes)
        )
    }

    // Act
    val clusters = dbscan(parisPoints, epsilon = 100.0, minPoints = 3)

    // Assert
    assertEquals(1, clusters.size)
    assertEquals(10, clusters[0].samples.size)
}
```

## Related Documentation

- `docs/LOCATION_TRACKING.md` - Location tracking implementation
- `shared/src/commonMain/kotlin/location/README.md` - Module docs

---

*Use this skill for implementing and optimizing location processing algorithms in Trailglass.*
