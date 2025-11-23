package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.BatchLocationRequest
import com.po4yka.trailglass.data.remote.dto.BatchLocationResponse
import com.po4yka.trailglass.data.remote.dto.CreatePlaceVisitRequest
import com.po4yka.trailglass.data.remote.dto.LocationsResponse
import com.po4yka.trailglass.data.remote.dto.PlaceVisitDto
import com.po4yka.trailglass.data.remote.dto.PlaceVisitResponse
import com.po4yka.trailglass.data.remote.dto.PlaceVisitsResponse
import com.po4yka.trailglass.data.remote.dto.UpdatePlaceVisitRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API client for location and place visit sync operations.
 * Handles uploading location batches, managing place visits, and route segments.
 */
internal class LocationSyncApi(
    private val baseUrl: String
) {
    // ========== Locations ==========

    suspend fun uploadLocationBatch(
        client: HttpClient,
        token: String,
        request: BatchLocationRequest
    ): BatchLocationResponse =
        client
            .post("$baseUrl${ApiEndpoints.LOCATIONS_BATCH}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<BatchLocationResponse>()

    suspend fun getLocations(
        client: HttpClient,
        token: String,
        startTime: String? = null,
        endTime: String? = null,
        minAccuracy: Double? = null,
        limit: Int = 1000,
        offset: Int = 0
    ): LocationsResponse =
        client
            .get("$baseUrl${ApiEndpoints.LOCATIONS}") {
                bearerAuth(token)
                parameter("startTime", startTime)
                parameter("endTime", endTime)
                parameter("minAccuracy", minAccuracy)
                parameter("limit", limit)
                parameter("offset", offset)
            }.body<LocationsResponse>()

    suspend fun deleteLocation(
        client: HttpClient,
        token: String,
        id: String
    ) {
        client.delete("$baseUrl${ApiEndpoints.locationById(id)}") {
            bearerAuth(token)
        }
    }

    // ========== Place Visits ==========

    suspend fun getPlaceVisits(
        client: HttpClient,
        token: String,
        startTime: String? = null,
        endTime: String? = null,
        category: String? = null,
        isFavorite: Boolean? = null,
        limit: Int = 100,
        offset: Int = 0
    ): PlaceVisitsResponse =
        client
            .get("$baseUrl${ApiEndpoints.PLACE_VISITS}") {
                bearerAuth(token)
                parameter("startTime", startTime)
                parameter("endTime", endTime)
                parameter("category", category)
                parameter("isFavorite", isFavorite)
                parameter("limit", limit)
                parameter("offset", offset)
            }.body<PlaceVisitsResponse>()

    suspend fun getPlaceVisit(
        client: HttpClient,
        token: String,
        id: String
    ): PlaceVisitDto =
        client
            .get("$baseUrl${ApiEndpoints.placeVisitById(id)}") {
                bearerAuth(token)
            }.body<PlaceVisitDto>()

    suspend fun createPlaceVisit(
        client: HttpClient,
        token: String,
        request: CreatePlaceVisitRequest
    ): PlaceVisitResponse =
        client
            .post("$baseUrl${ApiEndpoints.PLACE_VISITS}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<PlaceVisitResponse>()

    suspend fun updatePlaceVisit(
        client: HttpClient,
        token: String,
        id: String,
        request: UpdatePlaceVisitRequest
    ): PlaceVisitResponse =
        client
            .put("$baseUrl${ApiEndpoints.placeVisitById(id)}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<PlaceVisitResponse>()

    suspend fun deletePlaceVisit(
        client: HttpClient,
        token: String,
        id: String
    ) {
        client.delete("$baseUrl${ApiEndpoints.placeVisitById(id)}") {
            bearerAuth(token)
        }
    }
}
