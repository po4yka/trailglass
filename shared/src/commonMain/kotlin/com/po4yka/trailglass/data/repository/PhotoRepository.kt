package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PhotoAttachment
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Repository for photo data.
 */
interface PhotoRepository {
    /**
     * Insert or update a photo.
     */
    suspend fun insertPhoto(photo: Photo)

    /**
     * Get a photo by ID.
     */
    suspend fun getPhotoById(photoId: String): Photo?

    /**
     * Get all photos for a user.
     */
    suspend fun getPhotosForUser(userId: String): List<Photo>

    /**
     * Get photos within a time range.
     */
    suspend fun getPhotosInTimeRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<Photo>

    /**
     * Get photos for a specific day.
     */
    suspend fun getPhotosForDay(
        userId: String,
        date: LocalDate
    ): List<Photo>

    /**
     * Get photos near a location (within bounding box).
     *
     * @param minLat Minimum latitude
     * @param maxLat Maximum latitude
     * @param minLon Minimum longitude
     * @param maxLon Maximum longitude
     */
    suspend fun getPhotosNearLocation(
        userId: String,
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<Photo>

    /**
     * Delete a photo.
     */
    suspend fun deletePhoto(photoId: String)

    /**
     * Get total photo count for user.
     */
    suspend fun getPhotoCount(userId: String): Long

    // Photo Attachments

    /**
     * Attach a photo to a place visit.
     */
    suspend fun attachPhotoToVisit(attachment: PhotoAttachment)

    /**
     * Get attachments for a photo.
     */
    suspend fun getAttachmentsForPhoto(photoId: String): List<PhotoAttachment>

    /**
     * Get attachments for a visit.
     */
    suspend fun getAttachmentsForVisit(visitId: String): List<PhotoAttachment>

    /**
     * Get photos for a visit.
     */
    suspend fun getPhotosForVisit(visitId: String): List<Photo>

    /**
     * Check if a photo is attached to a visit.
     */
    suspend fun isPhotoAttachedToVisit(
        photoId: String,
        visitId: String
    ): Boolean

    /**
     * Delete an attachment.
     */
    suspend fun deleteAttachment(attachmentId: String)

    /**
     * Delete all attachments for a photo.
     */
    suspend fun deleteAttachmentsForPhoto(photoId: String)

    /**
     * Delete all attachments for a visit.
     */
    suspend fun deleteAttachmentsForVisit(visitId: String)
}
