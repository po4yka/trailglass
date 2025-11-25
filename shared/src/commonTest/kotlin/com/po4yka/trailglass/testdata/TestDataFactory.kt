package com.po4yka.trailglass.testdata

import com.po4yka.trailglass.domain.model.CategoryConfidence
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.domain.model.Note
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PhotoAttachment
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Test data factory for creating domain model instances in tests.
 *
 * Provides builder-style factory methods with sensible defaults.
 * All IDs are auto-generated if not specified.
 *
 * Example usage:
 * ```
 * val sample = TestDataFactory.locationSample()
 * val trip = TestDataFactory.trip(name = "Paris Trip")
 * val visit = TestDataFactory.placeVisit(
 *     poiName = "Eiffel Tower",
 *     latitude = 48.8584,
 *     longitude = 2.2945
 * )
 * ```
 */
@OptIn(ExperimentalUuidApi::class)
object TestDataFactory {
    // Default test values
    const val DEFAULT_USER_ID = "test_user_123"
    const val DEFAULT_DEVICE_ID = "test_device_456"
    const val DEFAULT_LATITUDE = 37.7749 // San Francisco
    const val DEFAULT_LONGITUDE = -122.4194
    const val DEFAULT_ACCURACY = 10.0

    private fun randomId(): String = Uuid.random().toString()
    private fun now(): Instant = Clock.System.now()

    // ==================== LocationSample ====================

    fun locationSample(
        id: String = randomId(),
        timestamp: Instant = now(),
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        altitude: Double? = null,
        accuracy: Double = DEFAULT_ACCURACY,
        speed: Double? = null,
        bearing: Double? = null,
        source: LocationSource = LocationSource.GPS,
        tripId: String? = null,
        uploadedAt: Instant? = null,
        deviceId: String = DEFAULT_DEVICE_ID,
        userId: String = DEFAULT_USER_ID
    ) = LocationSample(
        id = id,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        source = source,
        tripId = tripId,
        uploadedAt = uploadedAt,
        deviceId = deviceId,
        userId = userId
    )

    /**
     * Create a list of location samples along a path.
     *
     * @param count Number of samples to create
     * @param startLatitude Starting latitude
     * @param startLongitude Starting longitude
     * @param latitudeIncrement Latitude change per sample
     * @param longitudeIncrement Longitude change per sample
     * @param intervalMinutes Time interval between samples
     */
    fun locationSamplePath(
        count: Int,
        startLatitude: Double = DEFAULT_LATITUDE,
        startLongitude: Double = DEFAULT_LONGITUDE,
        latitudeIncrement: Double = 0.001,
        longitudeIncrement: Double = 0.001,
        intervalMinutes: Int = 5,
        tripId: String? = null,
        userId: String = DEFAULT_USER_ID
    ): List<LocationSample> {
        val startTime = now()
        return (0 until count).map { index ->
            locationSample(
                timestamp = startTime + (index * intervalMinutes).minutes,
                latitude = startLatitude + (index * latitudeIncrement),
                longitude = startLongitude + (index * longitudeIncrement),
                tripId = tripId,
                userId = userId
            )
        }
    }

    // ==================== Trip ====================

    fun trip(
        id: String = randomId(),
        name: String? = "Test Trip",
        startTime: Instant = now(),
        endTime: Instant? = now() + 24.hours,
        primaryCountry: String? = "US",
        isOngoing: Boolean = false,
        userId: String = DEFAULT_USER_ID,
        totalDistanceMeters: Double = 0.0,
        visitedPlaceCount: Int = 0,
        countriesVisited: List<String> = emptyList(),
        citiesVisited: List<String> = emptyList(),
        description: String? = null,
        coverPhotoUri: String? = null,
        isPublic: Boolean = false,
        tags: List<String> = emptyList(),
        isAutoDetected: Boolean = false,
        detectionConfidence: Float = 0f,
        createdAt: Instant? = now(),
        updatedAt: Instant? = now()
    ) = Trip(
        id = id,
        name = name,
        startTime = startTime,
        endTime = endTime,
        primaryCountry = primaryCountry,
        isOngoing = isOngoing,
        userId = userId,
        totalDistanceMeters = totalDistanceMeters,
        visitedPlaceCount = visitedPlaceCount,
        countriesVisited = countriesVisited,
        citiesVisited = citiesVisited,
        description = description,
        coverPhotoUri = coverPhotoUri,
        isPublic = isPublic,
        tags = tags,
        isAutoDetected = isAutoDetected,
        detectionConfidence = detectionConfidence,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun ongoingTrip(
        id: String = randomId(),
        name: String? = "Ongoing Trip",
        startTime: Instant = now() - 2.hours,
        userId: String = DEFAULT_USER_ID
    ) = trip(
        id = id,
        name = name,
        startTime = startTime,
        endTime = null,
        isOngoing = true,
        userId = userId
    )

    // ==================== PlaceVisit ====================

    fun placeVisit(
        id: String = randomId(),
        startTime: Instant = now() - 1.hours,
        endTime: Instant = now(),
        centerLatitude: Double = DEFAULT_LATITUDE,
        centerLongitude: Double = DEFAULT_LONGITUDE,
        approximateAddress: String? = "123 Test Street",
        poiName: String? = null,
        city: String? = "San Francisco",
        countryCode: String? = "US",
        locationSampleIds: List<String> = emptyList(),
        category: PlaceCategory = PlaceCategory.OTHER,
        categoryConfidence: CategoryConfidence = CategoryConfidence.LOW,
        significance: PlaceSignificance = PlaceSignificance.RARE,
        userLabel: String? = null,
        userNotes: String? = null,
        isFavorite: Boolean = false,
        frequentPlaceId: String? = null,
        userId: String? = DEFAULT_USER_ID,
        createdAt: Instant? = now(),
        updatedAt: Instant? = now()
    ) = PlaceVisit(
        id = id,
        startTime = startTime,
        endTime = endTime,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        approximateAddress = approximateAddress,
        poiName = poiName,
        city = city,
        countryCode = countryCode,
        locationSampleIds = locationSampleIds,
        category = category,
        categoryConfidence = categoryConfidence,
        significance = significance,
        userLabel = userLabel,
        userNotes = userNotes,
        isFavorite = isFavorite,
        frequentPlaceId = frequentPlaceId,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun homeVisit(
        id: String = randomId(),
        duration: Duration = 8.hours,
        userId: String = DEFAULT_USER_ID
    ): PlaceVisit {
        val end = now()
        return placeVisit(
            id = id,
            startTime = end - duration,
            endTime = end,
            poiName = "Home",
            category = PlaceCategory.HOME,
            categoryConfidence = CategoryConfidence.HIGH,
            significance = PlaceSignificance.PRIMARY,
            userId = userId
        )
    }

    fun workVisit(
        id: String = randomId(),
        duration: Duration = 8.hours,
        userId: String = DEFAULT_USER_ID
    ): PlaceVisit {
        val end = now()
        return placeVisit(
            id = id,
            startTime = end - duration,
            endTime = end,
            poiName = "Office",
            category = PlaceCategory.WORK,
            categoryConfidence = CategoryConfidence.HIGH,
            significance = PlaceSignificance.PRIMARY,
            userId = userId
        )
    }

    // ==================== FrequentPlace ====================

    fun frequentPlace(
        id: String = randomId(),
        centerLatitude: Double = DEFAULT_LATITUDE,
        centerLongitude: Double = DEFAULT_LONGITUDE,
        radiusMeters: Double = 50.0,
        name: String? = null,
        address: String? = "123 Test Street",
        city: String? = "San Francisco",
        countryCode: String? = "US",
        category: PlaceCategory = PlaceCategory.OTHER,
        categoryConfidence: CategoryConfidence = CategoryConfidence.LOW,
        significance: PlaceSignificance = PlaceSignificance.RARE,
        visitCount: Int = 1,
        totalDuration: Duration = 1.hours,
        firstVisitTime: Instant? = now() - 24.hours,
        lastVisitTime: Instant? = now(),
        userLabel: String? = null,
        userNotes: String? = null,
        isFavorite: Boolean = false,
        userId: String = DEFAULT_USER_ID,
        createdAt: Instant = now(),
        updatedAt: Instant = now()
    ) = FrequentPlace(
        id = id,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        radiusMeters = radiusMeters,
        name = name,
        address = address,
        city = city,
        countryCode = countryCode,
        category = category,
        categoryConfidence = categoryConfidence,
        significance = significance,
        visitCount = visitCount,
        totalDuration = totalDuration,
        firstVisitTime = firstVisitTime,
        lastVisitTime = lastVisitTime,
        userLabel = userLabel,
        userNotes = userNotes,
        isFavorite = isFavorite,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun homePlace(
        id: String = randomId(),
        userId: String = DEFAULT_USER_ID
    ) = frequentPlace(
        id = id,
        name = "Home",
        category = PlaceCategory.HOME,
        categoryConfidence = CategoryConfidence.HIGH,
        significance = PlaceSignificance.PRIMARY,
        visitCount = 100,
        totalDuration = 500.hours,
        userId = userId
    )

    fun workPlace(
        id: String = randomId(),
        userId: String = DEFAULT_USER_ID
    ) = frequentPlace(
        id = id,
        name = "Work",
        category = PlaceCategory.WORK,
        categoryConfidence = CategoryConfidence.HIGH,
        significance = PlaceSignificance.PRIMARY,
        visitCount = 80,
        totalDuration = 400.hours,
        userId = userId
    )

    // ==================== RouteSegment ====================

    fun routeSegment(
        id: String = randomId(),
        startTime: Instant = now() - 30.minutes,
        endTime: Instant = now(),
        fromPlaceVisitId: String? = null,
        toPlaceVisitId: String? = null,
        locationSampleIds: List<String> = emptyList(),
        simplifiedPath: List<Coordinate> = emptyList(),
        transportType: TransportType = TransportType.CAR,
        distanceMeters: Double = 5000.0,
        averageSpeedMps: Double? = 10.0,
        confidence: Float = 0.9f
    ) = RouteSegment(
        id = id,
        startTime = startTime,
        endTime = endTime,
        fromPlaceVisitId = fromPlaceVisitId,
        toPlaceVisitId = toPlaceVisitId,
        locationSampleIds = locationSampleIds,
        simplifiedPath = simplifiedPath,
        transportType = transportType,
        distanceMeters = distanceMeters,
        averageSpeedMps = averageSpeedMps,
        confidence = confidence
    )

    fun walkingRoute(
        id: String = randomId(),
        distanceMeters: Double = 500.0
    ) = routeSegment(
        id = id,
        transportType = TransportType.WALK,
        distanceMeters = distanceMeters,
        averageSpeedMps = 1.4 // ~5 km/h
    )

    fun drivingRoute(
        id: String = randomId(),
        distanceMeters: Double = 10000.0
    ) = routeSegment(
        id = id,
        transportType = TransportType.CAR,
        distanceMeters = distanceMeters,
        averageSpeedMps = 15.0 // ~54 km/h
    )

    // ==================== Region ====================

    fun region(
        id: String = randomId(),
        userId: String = DEFAULT_USER_ID,
        name: String = "Test Region",
        description: String? = null,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        radiusMeters: Int = Region.DEFAULT_RADIUS_METERS,
        notificationsEnabled: Boolean = true,
        createdAt: Instant = now(),
        updatedAt: Instant = now(),
        enterCount: Int = 0,
        lastEnterTime: Instant? = null,
        lastExitTime: Instant? = null
    ) = Region(
        id = id,
        userId = userId,
        name = name,
        description = description,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        notificationsEnabled = notificationsEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt,
        enterCount = enterCount,
        lastEnterTime = lastEnterTime,
        lastExitTime = lastExitTime
    )

    // ==================== Photo ====================

    fun photo(
        id: String = randomId(),
        uri: String = "content://media/external/images/media/123",
        timestamp: Instant = now(),
        latitude: Double? = DEFAULT_LATITUDE,
        longitude: Double? = DEFAULT_LONGITUDE,
        width: Int? = 1920,
        height: Int? = 1080,
        sizeBytes: Long? = 1024 * 1024,
        mimeType: String? = "image/jpeg",
        userId: String = DEFAULT_USER_ID,
        addedAt: Instant = now()
    ) = Photo(
        id = id,
        uri = uri,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        width = width,
        height = height,
        sizeBytes = sizeBytes,
        mimeType = mimeType,
        userId = userId,
        addedAt = addedAt
    )

    fun photoAttachment(
        id: String = randomId(),
        photoId: String = randomId(),
        placeVisitId: String = randomId(),
        attachedAt: Instant = now(),
        caption: String? = null
    ) = PhotoAttachment(
        id = id,
        photoId = photoId,
        placeVisitId = placeVisitId,
        attachedAt = attachedAt,
        caption = caption
    )

    // ==================== Note ====================

    fun note(
        id: String = randomId(),
        userId: String = DEFAULT_USER_ID,
        title: String = "Test Note",
        content: String = "This is a test note content.",
        location: Coordinate? = Coordinate(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
        timestamp: Instant = now(),
        createdAt: Instant = now(),
        updatedAt: Instant = now()
    ) = Note(
        id = id,
        userId = userId,
        title = title,
        content = content,
        location = location,
        timestamp = timestamp,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    // ==================== Coordinate ====================

    fun coordinate(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ) = Coordinate(latitude = latitude, longitude = longitude)

    /**
     * Famous locations for testing.
     */
    object Locations {
        val SAN_FRANCISCO = Coordinate(37.7749, -122.4194)
        val NEW_YORK = Coordinate(40.7128, -74.0060)
        val LONDON = Coordinate(51.5074, -0.1278)
        val PARIS = Coordinate(48.8566, 2.3522)
        val TOKYO = Coordinate(35.6762, 139.6503)
        val SYDNEY = Coordinate(-33.8688, 151.2093)

        // Landmarks
        val EIFFEL_TOWER = Coordinate(48.8584, 2.2945)
        val STATUE_OF_LIBERTY = Coordinate(40.6892, -74.0445)
        val GOLDEN_GATE_BRIDGE = Coordinate(37.8199, -122.4783)
    }
}
