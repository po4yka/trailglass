package com.po4yka.trailglass.data.remote

import com.po4yka.trailglass.data.remote.dto.PhotoAttachmentDto
import com.po4yka.trailglass.data.remote.dto.PhotoAttachmentsResponse
import com.po4yka.trailglass.data.remote.dto.PhotoMetadataDto
import com.po4yka.trailglass.data.remote.dto.PhotoUploadMetadata
import com.po4yka.trailglass.data.remote.dto.PhotoUploadResponse
import com.po4yka.trailglass.data.remote.dto.SyncPhotoAttachmentsRequest
import com.po4yka.trailglass.data.remote.dto.SyncPhotoAttachmentsResponse
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
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * API client for photo sync operations.
 * Handles uploading photos, managing photo metadata, and photo attachments.
 */
internal class PhotoSyncApi(
    private val baseUrl: String
) {
    /**
     * Upload a photo with metadata.
     *
     * @param client HTTP client
     * @param token Authentication token
     * @param photoId Photo identifier
     * @param file Photo file bytes
     * @param metadata Photo metadata
     * @return Photo upload response with URLs and server version
     */
    @OptIn(io.ktor.utils.io.InternalAPI::class)
    suspend fun uploadPhoto(
        client: HttpClient,
        token: String,
        photoId: String,
        file: ByteArray,
        metadata: PhotoUploadMetadata
    ): PhotoUploadResponse {
        val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
        val metadataJson = json.encodeToString(metadata)

        return client.post("$baseUrl${ApiEndpoints.PHOTOS_UPLOAD}") {
            bearerAuth(token)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", file, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"photo.jpg\"")
                        })
                        append("metadata", metadataJson, Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Application.Json)
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"metadata\"")
                        })
                    }
                )
            )
        }.body<PhotoUploadResponse>()
    }

    /**
     * Download a photo by ID.
     *
     * @param client HTTP client
     * @param token Authentication token
     * @param photoId Photo identifier
     * @return Photo file bytes
     */
    suspend fun downloadPhoto(
        client: HttpClient,
        token: String,
        photoId: String
    ): ByteArray {
        return client.get("$baseUrl${ApiEndpoints.photoDownload(photoId)}") {
            bearerAuth(token)
        }.body<ByteArray>()
    }

    /**
     * Get photo metadata by ID.
     *
     * @param client HTTP client
     * @param token Authentication token
     * @param photoId Photo identifier
     * @return Photo metadata
     */
    suspend fun getPhotoMetadata(
        client: HttpClient,
        token: String,
        photoId: String
    ): PhotoMetadataDto {
        return client.get("$baseUrl${ApiEndpoints.photoById(photoId)}") {
            bearerAuth(token)
        }.body<PhotoMetadataDto>()
    }

    /**
     * Delete a photo by ID.
     *
     * @param client HTTP client
     * @param token Authentication token
     * @param photoId Photo identifier
     */
    suspend fun deletePhoto(
        client: HttpClient,
        token: String,
        photoId: String
    ) {
        client.delete("$baseUrl${ApiEndpoints.photoById(photoId)}") {
            bearerAuth(token)
        }
    }

    /**
     * Sync photo attachments (create/update/delete).
     *
     * @param client HTTP client
     * @param token Authentication token
     * @param attachments List of photo attachments to sync
     * @return Synced photo attachments with server versions
     */
    suspend fun syncPhotoAttachments(
        client: HttpClient,
        token: String,
        attachments: List<PhotoAttachmentDto>
    ): SyncPhotoAttachmentsResponse {
        return client.post("$baseUrl${ApiEndpoints.PHOTOS_ATTACHMENTS}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(SyncPhotoAttachmentsRequest(attachments))
        }.body<SyncPhotoAttachmentsResponse>()
    }

    /**
     * Get photo attachments for a specific photo.
     *
     * @param client HTTP client
     * @param token Authentication token
     * @param photoId Photo identifier
     * @return List of photo attachments
     */
    suspend fun getAttachmentsForPhoto(
        client: HttpClient,
        token: String,
        photoId: String
    ): PhotoAttachmentsResponse {
        return client.get("$baseUrl${ApiEndpoints.photoAttachmentsByPhoto(photoId)}") {
            bearerAuth(token)
        }.body<PhotoAttachmentsResponse>()
    }

    /**
     * Get photo attachments for a specific place visit.
     *
     * @param client HTTP client
     * @param token Authentication token
     * @param visitId Place visit identifier
     * @return List of photo attachments
     */
    suspend fun getAttachmentsForVisit(
        client: HttpClient,
        token: String,
        visitId: String
    ): PhotoAttachmentsResponse {
        return client.get("$baseUrl${ApiEndpoints.photoAttachmentsByVisit(visitId)}") {
            bearerAuth(token)
        }.body<PhotoAttachmentsResponse>()
    }
}
