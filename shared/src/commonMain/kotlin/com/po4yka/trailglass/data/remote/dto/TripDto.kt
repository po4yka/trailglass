package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Trip data transfer object.
 */
@Serializable
data class TripDto(
    val id: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val placeVisits: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val notes: String? = null,
    val totalDistance: Double? = null,
    val countries: List<String> = emptyList(),
    val syncAction: SyncAction? = null,
    val localVersion: Long? = null,
    val serverVersion: Long? = null,
    val lastModified: String? = null,
    val deviceId: String? = null,
    val createdBy: String? = null
)

/**
 * Create trip request.
 */
@Serializable
data class CreateTripRequest(
    val id: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val notes: String? = null
)

/**
 * Update trip request.
 */
@Serializable
data class UpdateTripRequest(
    val name: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val notes: String? = null,
    val expectedVersion: Long
)

/**
 * Trip response (for create/update).
 */
@Serializable
data class TripResponse(
    val id: String,
    val serverVersion: Long,
    val syncTimestamp: String
)

/**
 * Trips query response.
 */
@Serializable
data class TripsResponse(
    val trips: List<TripDto>,
    val pagination: Pagination
)
