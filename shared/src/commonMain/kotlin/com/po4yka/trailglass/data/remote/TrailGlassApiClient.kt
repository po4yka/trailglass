package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.BatchLocationRequest
import com.po4yka.trailglass.data.remote.dto.BatchLocationResponse
import com.po4yka.trailglass.data.remote.dto.CreatePlaceVisitRequest
import com.po4yka.trailglass.data.remote.dto.CreateTripRequest
import com.po4yka.trailglass.data.remote.dto.DataExportRequest
import com.po4yka.trailglass.data.remote.dto.DataExportResponse
import com.po4yka.trailglass.data.remote.dto.DeltaSyncRequest
import com.po4yka.trailglass.data.remote.dto.DeltaSyncResponse
import com.po4yka.trailglass.data.remote.dto.DeviceInfoDto
import com.po4yka.trailglass.data.remote.dto.ExportStatusResponse
import com.po4yka.trailglass.data.remote.dto.LocationsResponse
import com.po4yka.trailglass.data.remote.dto.LoginRequest
import com.po4yka.trailglass.data.remote.dto.LoginResponse
import com.po4yka.trailglass.data.remote.dto.LogoutRequest
import com.po4yka.trailglass.data.remote.dto.PlaceVisitDto
import com.po4yka.trailglass.data.remote.dto.PlaceVisitResponse
import com.po4yka.trailglass.data.remote.dto.PlaceVisitsResponse
import com.po4yka.trailglass.data.remote.dto.RefreshTokenRequest
import com.po4yka.trailglass.data.remote.dto.RefreshTokenResponse
import com.po4yka.trailglass.data.remote.dto.RegisterRequest
import com.po4yka.trailglass.data.remote.dto.RegisterResponse
import com.po4yka.trailglass.data.remote.dto.ResolveConflictRequest
import com.po4yka.trailglass.data.remote.dto.ResolveConflictResponse
import com.po4yka.trailglass.data.remote.dto.SettingsDto
import com.po4yka.trailglass.data.remote.dto.SettingsResponse
import com.po4yka.trailglass.data.remote.dto.SyncStatusResponse
import com.po4yka.trailglass.data.remote.dto.TripResponse
import com.po4yka.trailglass.data.remote.dto.TripsResponse
import com.po4yka.trailglass.data.remote.dto.UpdatePlaceVisitRequest
import com.po4yka.trailglass.data.remote.dto.UpdateSettingsRequest
import com.po4yka.trailglass.data.remote.dto.UpdateTripRequest
import com.po4yka.trailglass.data.remote.dto.UpdateUserProfileRequest
import com.po4yka.trailglass.data.remote.dto.UpdateUserProfileResponse
import com.po4yka.trailglass.data.remote.dto.UserDevicesResponse
import com.po4yka.trailglass.data.remote.dto.UserProfileDto
import com.po4yka.trailglass.logging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/** Configuration for TrailGlass API client. */
data class ApiConfig(
    val baseUrl: String = "https://trailglass.po4yka.com/api/v1",
    val timeout: Long = 30000,
    val enableLogging: Boolean = true
)

/** Token provider interface for authentication. */
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

/** Device info provider interface. */
interface DeviceInfoProvider {
    fun getDeviceId(): String

    fun getDeviceName(): String

    fun getPlatform(): String

    fun getOsVersion(): String

    fun getAppVersion(): String
}

/**
 * Main API client for TrailGlass backend.
 * Coordinates all API operations by delegating to specialized API clients.
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

    // Specialized API clients
    private val locationSyncApi = LocationSyncApi(config.baseUrl)
    private val photoSyncApi = PhotoSyncApi(config.baseUrl)
    private val tripSyncApi = TripSyncApi(config.baseUrl)
    private val userDeviceApi = UserDeviceApi(config.baseUrl)

    /** Execute an authenticated request with automatic token refresh. */
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

    /** Refresh the access token using the refresh token. */
    private suspend fun refreshAccessToken(): Boolean {
        return try {
            val refreshToken = tokenProvider.getRefreshToken() ?: return false

            val response =
                client
                    .post("${config.baseUrl}${ApiEndpoints.AUTH_REFRESH}") {
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
                    .post("${config.baseUrl}${ApiEndpoints.AUTH_REGISTER}") {
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
                    .post("${config.baseUrl}${ApiEndpoints.AUTH_LOGIN}") {
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
            httpClient.post("${config.baseUrl}${ApiEndpoints.AUTH_LOGOUT}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(LogoutRequest(deviceId = deviceInfoProvider.getDeviceId()))
            }
            tokenProvider.clearTokens()
        }

    // ========== Sync ==========

    suspend fun getSyncStatus(): Result<SyncStatusResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.getSyncStatus(httpClient, token)
        }

    suspend fun performDeltaSync(request: DeltaSyncRequest): Result<DeltaSyncResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.performDeltaSync(httpClient, token, request)
        }

    suspend fun resolveConflict(request: ResolveConflictRequest): Result<ResolveConflictResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.resolveConflict(httpClient, token, request)
        }

    // ========== Locations ==========

    suspend fun uploadLocationBatch(request: BatchLocationRequest): Result<BatchLocationResponse> =
        authenticatedRequest { httpClient, token ->
            locationSyncApi.uploadLocationBatch(httpClient, token, request)
        }

    suspend fun getLocations(
        startTime: String? = null,
        endTime: String? = null,
        minAccuracy: Double? = null,
        limit: Int = 1000,
        offset: Int = 0
    ): Result<LocationsResponse> =
        authenticatedRequest { httpClient, token ->
            locationSyncApi.getLocations(httpClient, token, startTime, endTime, minAccuracy, limit, offset)
        }

    suspend fun deleteLocation(id: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            locationSyncApi.deleteLocation(httpClient, token, id)
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
            locationSyncApi.getPlaceVisits(httpClient, token, startTime, endTime, category, isFavorite, limit, offset)
        }

    suspend fun getPlaceVisit(id: String): Result<PlaceVisitDto> =
        authenticatedRequest { httpClient, token ->
            locationSyncApi.getPlaceVisit(httpClient, token, id)
        }

    suspend fun createPlaceVisit(request: CreatePlaceVisitRequest): Result<PlaceVisitResponse> =
        authenticatedRequest { httpClient, token ->
            locationSyncApi.createPlaceVisit(httpClient, token, request)
        }

    suspend fun updatePlaceVisit(
        id: String,
        request: UpdatePlaceVisitRequest
    ): Result<PlaceVisitResponse> =
        authenticatedRequest { httpClient, token ->
            locationSyncApi.updatePlaceVisit(httpClient, token, id, request)
        }

    suspend fun deletePlaceVisit(id: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            locationSyncApi.deletePlaceVisit(httpClient, token, id)
        }

    // ========== Trips ==========

    suspend fun getTrips(
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<TripsResponse> =
        authenticatedRequest { httpClient, token ->
            tripSyncApi.getTrips(httpClient, token, startDate, endDate, limit, offset)
        }

    suspend fun createTrip(request: CreateTripRequest): Result<TripResponse> =
        authenticatedRequest { httpClient, token ->
            tripSyncApi.createTrip(httpClient, token, request)
        }

    suspend fun updateTrip(
        id: String,
        request: UpdateTripRequest
    ): Result<TripResponse> =
        authenticatedRequest { httpClient, token ->
            tripSyncApi.updateTrip(httpClient, token, id, request)
        }

    suspend fun deleteTrip(id: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            tripSyncApi.deleteTrip(httpClient, token, id)
        }

    // ========== Settings ==========

    suspend fun getSettings(): Result<SettingsDto> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.getSettings(httpClient, token)
        }

    suspend fun updateSettings(request: UpdateSettingsRequest): Result<SettingsResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.updateSettings(httpClient, token, request)
        }

    // ========== User Profile ==========

    suspend fun getUserProfile(): Result<UserProfileDto> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.getUserProfile(httpClient, token)
        }

    suspend fun updateUserProfile(request: UpdateUserProfileRequest): Result<UpdateUserProfileResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.updateUserProfile(httpClient, token, request)
        }

    suspend fun getUserDevices(): Result<UserDevicesResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.getUserDevices(httpClient, token)
        }

    suspend fun deleteUserDevice(deviceId: String): Result<Unit> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.deleteUserDevice(httpClient, token, deviceId)
        }

    // ========== Data Export ==========

    suspend fun requestExport(request: DataExportRequest): Result<DataExportResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.requestExport(httpClient, token, request)
        }

    suspend fun getExportStatus(exportId: String): Result<ExportStatusResponse> =
        authenticatedRequest { httpClient, token ->
            userDeviceApi.getExportStatus(httpClient, token, exportId)
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
