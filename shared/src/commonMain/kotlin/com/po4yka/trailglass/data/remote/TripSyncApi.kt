package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.CreateTripRequest
import com.po4yka.trailglass.data.remote.dto.TripResponse
import com.po4yka.trailglass.data.remote.dto.TripsResponse
import com.po4yka.trailglass.data.remote.dto.UpdateTripRequest
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
 * API client for trip sync operations.
 * Handles creating, updating, and managing multi-day trips.
 */
internal class TripSyncApi(
    private val baseUrl: String
) {
    suspend fun getTrips(
        client: HttpClient,
        token: String,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): TripsResponse =
        client
            .get("$baseUrl${ApiEndpoints.TRIPS}") {
                bearerAuth(token)
                parameter("startDate", startDate)
                parameter("endDate", endDate)
                parameter("limit", limit)
                parameter("offset", offset)
            }.body<TripsResponse>()

    suspend fun createTrip(
        client: HttpClient,
        token: String,
        request: CreateTripRequest
    ): TripResponse =
        client
            .post("$baseUrl${ApiEndpoints.TRIPS}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<TripResponse>()

    suspend fun updateTrip(
        client: HttpClient,
        token: String,
        id: String,
        request: UpdateTripRequest
    ): TripResponse =
        client
            .put("$baseUrl${ApiEndpoints.tripById(id)}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<TripResponse>()

    suspend fun deleteTrip(
        client: HttpClient,
        token: String,
        id: String
    ) {
        client.delete("$baseUrl${ApiEndpoints.tripById(id)}") {
            bearerAuth(token)
        }
    }
}
