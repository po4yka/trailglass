package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/** Coordinate DTO. */
@Serializable
data class CoordinateDto(
    val latitude: Double,
    val longitude: Double
)

/** Place visit data transfer object. */
@Serializable
data class PlaceVisitDto(
    val id: String,
    val location: CoordinateDto,
    val placeName: String?,
    val address: String? = null,
    val category: String,
    val arrivalTime: String,
    val departureTime: String? = null,
    val durationMinutes: Int? = null,
    val confidence: Double,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val photos: List<String> = emptyList(),
    val tripId: String? = null,
    val syncAction: SyncAction? = null,
    val localVersion: Long? = null,
    val serverVersion: Long? = null,
    val lastModified: String? = null,
    val deviceId: String? = null,
    val createdBy: String? = null
)

/** Create place visit request. */
@Serializable
data class CreatePlaceVisitRequest(
    val id: String,
    val location: CoordinateDto,
    val placeName: String?,
    val address: String? = null,
    val category: String,
    val arrivalTime: String,
    val departureTime: String? = null,
    val confidence: Double,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val photos: List<String> = emptyList()
)

/** Update place visit request. */
@Serializable
data class UpdatePlaceVisitRequest(
    val placeName: String? = null,
    val address: String? = null,
    val category: String? = null,
    val departureTime: String? = null,
    val isFavorite: Boolean? = null,
    val notes: String? = null,
    val photos: List<String>? = null,
    val expectedVersion: Long
)

/** Place visit response (for create/update). */
@Serializable
data class PlaceVisitResponse(
    val id: String,
    val serverVersion: Long,
    val syncTimestamp: String
)

/** Place visits query response. */
@Serializable
data class PlaceVisitsResponse(
    val placeVisits: List<PlaceVisitDto>,
    val pagination: Pagination
)
