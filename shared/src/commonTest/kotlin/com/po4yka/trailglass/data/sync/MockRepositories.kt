package com.po4yka.trailglass.data.sync

import com.po4yka.trailglass.domain.model.Place
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Mock PlaceVisitRepository for testing.
 */
class MockPlaceVisitRepository : PlaceVisitRepository {
    private val visits = mutableMapOf<String, PlaceVisit>()

    override suspend fun insertVisit(visit: PlaceVisit) {
        visits[visit.id] = visit
    }

    override suspend fun getVisitById(id: String): PlaceVisit? = visits[id]

    override suspend fun getAllVisits(): List<PlaceVisit> = visits.values.toList()

    override suspend fun getVisitsByPlaceId(placeId: String): List<PlaceVisit> {
        return visits.values.filter { it.place.id == placeId }
    }

    override suspend fun getVisitsByTimeRange(start: Instant, end: Instant): List<PlaceVisit> {
        return visits.values.filter { it.arrivalTime >= start && it.arrivalTime <= end }
    }

    override suspend fun observeAllVisits(): Flow<List<PlaceVisit>> = flowOf(visits.values.toList())

    override suspend fun deleteVisit(visitId: String) {
        visits.remove(visitId)
    }

    override suspend fun deleteAllVisits() {
        visits.clear()
    }

    // Test helpers
    fun addVisit(visit: PlaceVisit) {
        visits[visit.id] = visit
    }

    fun getVisitCount(): Int = visits.size
}

/**
 * Mock TripRepository for testing.
 */
class MockTripRepository : TripRepository {
    private val trips = mutableMapOf<String, Trip>()

    override suspend fun insertTrip(trip: Trip) {
        trips[trip.id] = trip
    }

    override suspend fun getTripById(id: String): Trip? = trips[id]

    override suspend fun getAllTrips(): List<Trip> = trips.values.toList()

    override suspend fun getTripsByTimeRange(start: Instant, end: Instant): List<Trip> {
        return trips.values.filter { it.startTime >= start && it.startTime <= end }
    }

    override suspend fun observeAllTrips(): Flow<List<Trip>> = flowOf(trips.values.toList())

    override suspend fun deleteTrip(tripId: String) {
        trips.remove(tripId)
    }

    override suspend fun deleteAllTrips() {
        trips.clear()
    }

    // Test helpers
    fun addTrip(trip: Trip) {
        trips[trip.id] = trip
    }

    fun getTripCount(): Int = trips.size
}

// Test data generators
fun createMockPlaceVisit(
    id: String = "visit_${System.currentTimeMillis()}",
    placeName: String = "Test Place"
): PlaceVisit {
    return PlaceVisit(
        id = id,
        place = Place(
            id = "place_${id}",
            name = placeName,
            latitude = 37.7749,
            longitude = -122.4194,
            address = "123 Test St",
            category = "Restaurant"
        ),
        arrivalTime = Clock.System.now(),
        departureTime = null,
        durationMinutes = null
    )
}

fun createMockTrip(
    id: String = "trip_${System.currentTimeMillis()}",
    name: String = "Test Trip"
): Trip {
    return Trip(
        id = id,
        name = name,
        startTime = Clock.System.now(),
        endTime = null,
        totalDistanceMeters = 0.0,
        notes = null
    )
}
