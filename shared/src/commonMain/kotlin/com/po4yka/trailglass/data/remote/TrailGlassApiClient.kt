package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.*
import com.po4yka.trailglass.logging.logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/**
 * Configuration for TrailGlass API client.
 */
data class ApiConfig(
    val baseUrl: String = "https://trailglass.po4yka.com/api/v1",
    val timeout: Long = 30000,
    val enableLogging: Boolean = true
)

/**
 * Token provider interface for authentication.
 */
interface TokenProvider {
    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    )

    suspend fun clearTokens()
}

/**
 * Device info provider interface.
 */
interface DeviceInfoProvider {
    fun getDeviceId(): String

    fun getDeviceName(): String

    fun getPlatform(): String

    fun getOsVersion(): String

    fun getAppVersion(): String
}

/**
 * Main API client for TrailGlass backend.
 */
@Inject
class TrailGlassApiClient(
    private val config: ApiConfig,
    private val tokenProvider: TokenProvider,
    private val deviceInfoProvider: DeviceInfoProvider
) {
    private val logger = logger()

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = config.enableLogging
        }

    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            if (config.enableLogging) {
                install(Logging) {
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                this@TrailGlassApiClient.logger.debug { message }
                            }
                        }
                    level = LogLevel.BODY
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = config.timeout
                connectTimeoutMillis = config.timeout
                socketTimeoutMillis = config.timeout
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("X-Device-ID", deviceInfoProvider.getDeviceId())
                header("X-App-Version", deviceInfoProvider.getAppVersion())
            }

            // Auto-retry with exponential backoff
            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { _, response ->
                    response.status.value >= 500
                }
                exponentialDelay(base = 2.0, maxDelayMs = 10000)
            }
        }

    /**
     * Execute an authenticated request with automatic token refresh.
     */
    private suspend inline fun <reified T> authenticatedRequest(
        crossinline block: suspend (HttpClient, String) -> T
    ): Result<T> {
        return try {
            val token =
                tokenProvider.getAccessToken()
                    ?: return Result.failure(Exception("No access token available"))

            try {
                val result = block(client, token)
                Result.success(result)
            } catch (e: ClientRequestException) {
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    // Try to refresh token
                    val refreshed = refreshAccessToken()
                    if (refreshed) {
                        val newToken = tokenProvider.getAccessToken()!!
                        val result = block(client, newToken)
                        Result.success(result)
                    } else {
                        Result.failure(Exception("Authentication failed"))
                    }
                } else {
                    Result.failure(e)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "API request failed" }
            Result.failure(e)
        }
    }

    /**
     * Refresh the access token using the refresh token.
     */
    private suspend fun refreshAccessToken(): Boolean {
        return try {
            val refreshToken = tokenProvider.getRefreshToken() ?: return false

            val response =
                client
                    .post("${config.baseUrl}/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshTokenRequest(refreshToken))
                    }.body<RefreshTokenResponse>()

            tokenProvider.saveTokens(
                response.accessToken,
                response.refreshToken,
                response.expiresIn
            )
            true
        } catch (e: Exception) {
            logger.error(e) { "Token refresh failed" }
            false
        }
    }

    // ========== Authentication ==========

    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<RegisterResponse> =
        try {
            val response =
                client
                    .post("${config.baseUrl}/auth/register") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            RegisterRequest(
                                email = email,
                                password = password,
                                displayName = displayName,
                                deviceInfo = getDeviceInfo()
                            )
                        )
                    }.body<RegisterResponse>()

            // Save tokens
            tokenProvider.saveTokens(
                response.accessToken,
                response.refreshToken,
                response.expiresIn
            )

            Result.success(response)
        } catch (e: Exception) {
            logger.error(e) { "Registration failed" }
            Result.failure(e)
        }

    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponse> =
        try {
            val response =
                client
                    .post("${config.baseUrl}/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            LoginRequest(
                                email = email,
                                password = password,
                                deviceInfo = getDeviceInfo()
                            )
                        )
                    }.body<LoginResponse>()

            // Save tokens
            tokenProvider.saveTokens(
                response.accessToken,
                response.refreshToken,
                response.expiresIn
            )

            Result.success(response)
        } catch (e: Exception) {
            logger.error(e) { "Login failed" }
            Result.failure(e)
        }

    suspend fun logout(): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            httpClient.post("${config.baseUrl}/auth/logout") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(LogoutRequest(deviceId = deviceInfoProvider.getDeviceId()))
            }
            tokenProvider.clearTokens()
        }

    // ========== Sync ==========

    suspend fun getSyncStatus(): Result<SyncStatusResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/sync/status") {
                    bearerAuth(token)
                }.body<SyncStatusResponse>()
        }

    suspend fun performDeltaSync(request: DeltaSyncRequest): Result<DeltaSyncResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .post("${config.baseUrl}/sync/delta") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<DeltaSyncResponse>()
        }

    suspend fun resolveConflict(request: ResolveConflictRequest): Result<ResolveConflictResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .post("${config.baseUrl}/sync/resolve-conflict") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<ResolveConflictResponse>()
        }

    // ========== Locations ==========

    suspend fun uploadLocationBatch(request: BatchLocationRequest): Result<BatchLocationResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .post("${config.baseUrl}/locations/batch") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<BatchLocationResponse>()
        }

    suspend fun getLocations(
        startTime: String? = null,
        endTime: String? = null,
        minAccuracy: Double? = null,
        limit: Int = 1000,
        offset: Int = 0
    ): Result<LocationsResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/locations") {
                    bearerAuth(token)
                    parameter("startTime", startTime)
                    parameter("endTime", endTime)
                    parameter("minAccuracy", minAccuracy)
                    parameter("limit", limit)
                    parameter("offset", offset)
                }.body<LocationsResponse>()
        }

    suspend fun deleteLocation(id: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            httpClient.delete("${config.baseUrl}/locations/$id") {
                bearerAuth(token)
            }
        }

    // ========== Place Visits ==========

    suspend fun getPlaceVisits(
        startTime: String? = null,
        endTime: String? = null,
        category: String? = null,
        isFavorite: Boolean? = null,
        limit: Int = 100,
        offset: Int = 0
    ): Result<PlaceVisitsResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/place-visits") {
                    bearerAuth(token)
                    parameter("startTime", startTime)
                    parameter("endTime", endTime)
                    parameter("category", category)
                    parameter("isFavorite", isFavorite)
                    parameter("limit", limit)
                    parameter("offset", offset)
                }.body<PlaceVisitsResponse>()
        }

    suspend fun getPlaceVisit(id: String): Result<PlaceVisitDto> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/place-visits/$id") {
                    bearerAuth(token)
                }.body<PlaceVisitDto>()
        }

    suspend fun createPlaceVisit(request: CreatePlaceVisitRequest): Result<PlaceVisitResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .post("${config.baseUrl}/place-visits") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaceVisitResponse>()
        }

    suspend fun updatePlaceVisit(
        id: String,
        request: UpdatePlaceVisitRequest
    ): Result<PlaceVisitResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .put("${config.baseUrl}/place-visits/$id") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaceVisitResponse>()
        }

    suspend fun deletePlaceVisit(id: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            httpClient.delete("${config.baseUrl}/place-visits/$id") {
                bearerAuth(token)
            }
        }

    // ========== Trips ==========

    suspend fun getTrips(
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<TripsResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/trips") {
                    bearerAuth(token)
                    parameter("startDate", startDate)
                    parameter("endDate", endDate)
                    parameter("limit", limit)
                    parameter("offset", offset)
                }.body<TripsResponse>()
        }

    suspend fun createTrip(request: CreateTripRequest): Result<TripResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .post("${config.baseUrl}/trips") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<TripResponse>()
        }

    suspend fun updateTrip(
        id: String,
        request: UpdateTripRequest
    ): Result<TripResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .put("${config.baseUrl}/trips/$id") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<TripResponse>()
        }

    suspend fun deleteTrip(id: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            httpClient.delete("${config.baseUrl}/trips/$id") {
                bearerAuth(token)
            }
        }

    // ========== Settings ==========

    suspend fun getSettings(): Result<SettingsDto> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/settings") {
                    bearerAuth(token)
                }.body<SettingsDto>()
        }

    suspend fun updateSettings(request: UpdateSettingsRequest): Result<SettingsResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .put("${config.baseUrl}/settings") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<SettingsResponse>()
        }

    // ========== User Profile ==========

    suspend fun getUserProfile(): Result<UserProfileDto> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/user/profile") {
                    bearerAuth(token)
                }.body<UserProfileDto>()
        }

    suspend fun updateUserProfile(request: UpdateUserProfileRequest): Result<UpdateUserProfileResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .put("${config.baseUrl}/user/profile") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<UpdateUserProfileResponse>()
        }

    suspend fun getUserDevices(): Result<UserDevicesResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/user/devices") {
                    bearerAuth(token)
                }.body<UserDevicesResponse>()
        }

    suspend fun deleteUserDevice(deviceId: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            httpClient.delete("${config.baseUrl}/user/devices/$deviceId") {
                bearerAuth(token)
            }
        }

    // ========== Data Export ==========

    suspend fun requestExport(request: DataExportRequest): Result<DataExportResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .post("${config.baseUrl}/export/request") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<DataExportResponse>()
        }

    suspend fun getExportStatus(exportId: String): Result<ExportStatusResponse> =
        authenticatedRequest { httpClient, token ->
            httpClient
                .get("${config.baseUrl}/export/$exportId/status") {
                    bearerAuth(token)
                }.body<ExportStatusResponse>()
        }

    // ========== Helper Methods ==========

    private fun getDeviceInfo(): DeviceInfoDto =
        DeviceInfoDto(
            deviceId = deviceInfoProvider.getDeviceId(),
            deviceName = deviceInfoProvider.getDeviceName(),
            platform = deviceInfoProvider.getPlatform(),
            osVersion = deviceInfoProvider.getOsVersion(),
            appVersion = deviceInfoProvider.getAppVersion()
        )

    fun close() {
        client.close()
    }
}
