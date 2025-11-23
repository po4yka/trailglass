package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.DataExportRequest
import com.po4yka.trailglass.data.remote.dto.DataExportResponse
import com.po4yka.trailglass.data.remote.dto.DeltaSyncRequest
import com.po4yka.trailglass.data.remote.dto.DeltaSyncResponse
import com.po4yka.trailglass.data.remote.dto.ExportStatusResponse
import com.po4yka.trailglass.data.remote.dto.ResolveConflictRequest
import com.po4yka.trailglass.data.remote.dto.ResolveConflictResponse
import com.po4yka.trailglass.data.remote.dto.SettingsDto
import com.po4yka.trailglass.data.remote.dto.SettingsResponse
import com.po4yka.trailglass.data.remote.dto.SyncStatusResponse
import com.po4yka.trailglass.data.remote.dto.UpdateSettingsRequest
import com.po4yka.trailglass.data.remote.dto.UpdateUserProfileRequest
import com.po4yka.trailglass.data.remote.dto.UpdateUserProfileResponse
import com.po4yka.trailglass.data.remote.dto.UserDevicesResponse
import com.po4yka.trailglass.data.remote.dto.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API client for user, device, sync, and settings management.
 * Handles user profiles, device registration, sync operations, settings, and data export.
 */
internal class UserDeviceApi(
    private val baseUrl: String
) {
    // ========== Sync ==========

    suspend fun getSyncStatus(
        client: HttpClient,
        token: String
    ): SyncStatusResponse =
        client
            .get("$baseUrl${ApiEndpoints.SYNC_STATUS}") {
                bearerAuth(token)
            }.body<SyncStatusResponse>()

    suspend fun performDeltaSync(
        client: HttpClient,
        token: String,
        request: DeltaSyncRequest
    ): DeltaSyncResponse =
        client
            .post("$baseUrl${ApiEndpoints.SYNC_DELTA}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<DeltaSyncResponse>()

    suspend fun resolveConflict(
        client: HttpClient,
        token: String,
        request: ResolveConflictRequest
    ): ResolveConflictResponse =
        client
            .post("$baseUrl${ApiEndpoints.SYNC_RESOLVE_CONFLICT}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<ResolveConflictResponse>()

    // ========== Settings ==========

    suspend fun getSettings(
        client: HttpClient,
        token: String
    ): SettingsDto =
        client
            .get("$baseUrl${ApiEndpoints.SETTINGS}") {
                bearerAuth(token)
            }.body<SettingsDto>()

    suspend fun updateSettings(
        client: HttpClient,
        token: String,
        request: UpdateSettingsRequest
    ): SettingsResponse =
        client
            .put("$baseUrl${ApiEndpoints.SETTINGS}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<SettingsResponse>()

    // ========== User Profile ==========

    suspend fun getUserProfile(
        client: HttpClient,
        token: String
    ): UserProfileDto =
        client
            .get("$baseUrl${ApiEndpoints.USER_PROFILE}") {
                bearerAuth(token)
            }.body<UserProfileDto>()

    suspend fun updateUserProfile(
        client: HttpClient,
        token: String,
        request: UpdateUserProfileRequest
    ): UpdateUserProfileResponse =
        client
            .put("$baseUrl${ApiEndpoints.USER_PROFILE}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<UpdateUserProfileResponse>()

    suspend fun getUserDevices(
        client: HttpClient,
        token: String
    ): UserDevicesResponse =
        client
            .get("$baseUrl${ApiEndpoints.USER_DEVICES}") {
                bearerAuth(token)
            }.body<UserDevicesResponse>()

    suspend fun deleteUserDevice(
        client: HttpClient,
        token: String,
        deviceId: String
    ) {
        client.delete("$baseUrl${ApiEndpoints.userDeviceById(deviceId)}") {
            bearerAuth(token)
        }
    }

    // ========== Data Export ==========

    suspend fun requestExport(
        client: HttpClient,
        token: String,
        request: DataExportRequest
    ): DataExportResponse =
        client
            .post("$baseUrl${ApiEndpoints.EXPORT_REQUEST}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<DataExportResponse>()

    suspend fun getExportStatus(
        client: HttpClient,
        token: String,
        exportId: String
    ): ExportStatusResponse =
        client
            .get("$baseUrl${ApiEndpoints.exportStatus(exportId)}") {
                bearerAuth(token)
            }.body<ExportStatusResponse>()
}
