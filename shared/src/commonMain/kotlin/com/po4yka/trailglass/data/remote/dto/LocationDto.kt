package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Location data transfer object.
 */
@Serializable
data class LocationDto(
    val id: String,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Double,
    val speed: Double? = null,
    val bearing: Double? = null,
    val provider: String,
    val batteryLevel: Double? = null,
    val clientTimestamp: String? = null,
    val syncAction: SyncAction? = null,
    val localVersion: Long? = null,
    val serverVersion: Long? = null,
    val deviceId: String? = null
)

/**
 * Batch location upload request.
 */
@Serializable
data class BatchLocationRequest(
    val locations: List<LocationDto>,
    val deviceId: String
)

/**
 * Batch location upload response.
 */
@Serializable
data class BatchLocationResponse(
    val accepted: Int,
    val rejected: Int,
    val duplicates: Int,
    val syncVersion: Long
)

/**
 * Pagination metadata.
 */
@Serializable
data class Pagination(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val hasMore: Boolean
)

/**
 * Locations query response.
 */
@Serializable
data class LocationsResponse(
    val locations: List<LocationDto>,
    val pagination: Pagination
)
