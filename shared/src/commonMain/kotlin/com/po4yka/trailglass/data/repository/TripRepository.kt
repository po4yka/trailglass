package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.Instant

/** Repository for Trip data. */
interface TripRepository {
    /** Insert or update a trip. */
    suspend fun upsertTrip(trip: Trip): Result<Unit>

    /** Get a trip by ID. */
    suspend fun getTripById(tripId: String): Trip?

    /** Get all trips for a user. */
    suspend fun getTripsForUser(userId: String): List<Trip>

    /** Get ongoing trips for a user. */
    suspend fun getOngoingTrips(userId: String): List<Trip>

    /** Get trips within a time range. */
    suspend fun getTripsInRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<Trip>

    /** Mark a trip as complete (no longer ongoing). */
    suspend fun completeTrip(
        tripId: String,
        endTime: Instant
    )

    /** Delete a trip. */
    suspend fun deleteTrip(tripId: String): Result<Unit>

    /** Get the total number of trips for a user. */
    suspend fun getTripCount(userId: String): Long
}
