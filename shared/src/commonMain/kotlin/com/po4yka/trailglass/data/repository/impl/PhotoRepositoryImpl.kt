package com.po4yka.trailglass.data.repository.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.resultOf
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PhotoAttachment
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.days

/** SQLDelight implementation of PhotoRepository. */
@Inject
class PhotoRepositoryImpl(
    private val database: Database,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) : PhotoRepository {
    private val logger = logger()
    private val queries = database.photosQueries

    override suspend fun insertPhoto(photo: Photo): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Inserting photo: ${photo.id}" }
                queries.insertPhoto(
                    id = photo.id,
                    uri = photo.uri,
                    timestamp = photo.timestamp.toEpochMilliseconds(),
                    latitude = photo.latitude,
                    longitude = photo.longitude,
                    width = photo.width?.toLong(),
                    height = photo.height?.toLong(),
                    size_bytes = photo.sizeBytes,
                    mime_type = photo.mimeType,
                    user_id = photo.userId,
                    added_at = photo.addedAt.toEpochMilliseconds()
                )
                logger.trace { "Successfully inserted photo ${photo.id}" }
            }
        }

    override suspend fun getPhotoById(photoId: String): Photo? =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting photo by ID: $photoId" }
            try {
                queries
                    .getPhotoById(photoId)
                    .asFlow()
                    .mapToOneOrNull(Dispatchers.IO)
                    .first()
                    ?.let { mapToPhoto(it) }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get photo $photoId" }
                throw e
            }
        }

    override suspend fun getPhotosForUser(userId: String): List<Photo> =
        withContext(Dispatchers.IO) {
            logger.debug { "Getting all photos for user $userId" }
            try {
                queries
                    .getPhotosForUser(userId)
                    .asFlow()
                    .mapToList(Dispatchers.IO)
                    .first()
                    .map { mapToPhoto(it) }
                    .also { photos ->
                        logger.info { "Found ${photos.size} photos for user $userId" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get photos for user $userId" }
                throw e
            }
        }

    override suspend fun getPhotosInTimeRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<Photo> =
        withContext(Dispatchers.IO) {
            logger.debug { "Getting photos for user $userId in range $startTime to $endTime" }
            try {
                queries
                    .getPhotosInTimeRange(
                        user_id = userId,
                        timestamp = startTime.toEpochMilliseconds(),
                        timestamp_ = endTime.toEpochMilliseconds()
                    ).asFlow()
                    .mapToList(Dispatchers.IO)
                    .first()
                    .map { mapToPhoto(it) }
                    .also { photos ->
                        logger.debug { "Found ${photos.size} photos in time range" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get photos in time range" }
                throw e
            }
        }

    override suspend fun getPhotosForDay(
        userId: String,
        date: LocalDate
    ): List<Photo> =
        withContext(Dispatchers.IO) {
            val dayStart = date.atStartOfDayIn(timeZone)
            val dayEnd = dayStart + 1.days

            logger.debug { "Getting photos for user $userId on $date" }
            try {
                queries
                    .getPhotosForDay(
                        user_id = userId,
                        timestamp = dayStart.toEpochMilliseconds(),
                        timestamp_ = dayEnd.toEpochMilliseconds()
                    ).asFlow()
                    .mapToList(Dispatchers.IO)
                    .first()
                    .map { mapToPhoto(it) }
                    .also { photos ->
                        logger.debug { "Found ${photos.size} photos for $date" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get photos for day $date" }
                throw e
            }
        }

    override suspend fun getPhotosNearLocation(
        userId: String,
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<Photo> =
        withContext(Dispatchers.IO) {
            logger.debug { "Getting photos near location for user $userId" }
            try {
                queries
                    .getPhotosNearLocation(
                        user_id = userId,
                        latitude = minLat,
                        latitude_ = maxLat,
                        longitude = minLon,
                        longitude_ = maxLon
                    ).asFlow()
                    .mapToList(Dispatchers.IO)
                    .first()
                    .map { mapToPhoto(it) }
                    .also { photos ->
                        logger.debug { "Found ${photos.size} photos near location" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get photos near location" }
                throw e
            }
        }

    override suspend fun deletePhoto(photoId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Deleting photo $photoId" }
                queries.deletePhoto(photoId)
                logger.info { "Photo $photoId deleted" }
            }
        }

    override suspend fun getPhotoCount(userId: String): Long =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting photo count for user $userId" }
            try {
                queries.countPhotosForUser(userId).executeAsOne()
            } catch (e: Exception) {
                logger.error(e) { "Failed to get photo count for user $userId" }
                throw e
            }
        }

    // Photo Attachments

    override suspend fun attachPhotoToVisit(attachment: PhotoAttachment): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Attaching photo ${attachment.photoId} to visit ${attachment.placeVisitId}" }
                queries.insertAttachment(
                    id = attachment.id,
                    photo_id = attachment.photoId,
                    place_visit_id = attachment.placeVisitId,
                    attached_at = attachment.attachedAt.toEpochMilliseconds(),
                    caption = attachment.caption
                )
                logger.trace { "Successfully attached photo ${attachment.photoId} to visit ${attachment.placeVisitId}" }
            }
        }

    override suspend fun getAttachmentsForPhoto(photoId: String): List<PhotoAttachment> =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting attachments for photo $photoId" }
            try {
                queries
                    .getAttachmentsForPhoto(photoId)
                    .asFlow()
                    .mapToList(Dispatchers.IO)
                    .first()
                    .map { mapToPhotoAttachment(it) }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get attachments for photo $photoId" }
                throw e
            }
        }

    override suspend fun getAttachmentsForVisit(visitId: String): List<PhotoAttachment> =
        withContext(Dispatchers.IO) {
            logger.trace { "Getting attachments for visit $visitId" }
            try {
                queries
                    .getAttachmentsForVisit(visitId)
                    .asFlow()
                    .mapToList(Dispatchers.IO)
                    .first()
                    .map { mapToPhotoAttachment(it) }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get attachments for visit $visitId" }
                throw e
            }
        }

    override suspend fun getPhotosForVisit(visitId: String): List<Photo> =
        withContext(Dispatchers.IO) {
            logger.debug { "Getting photos for visit $visitId" }
            try {
                queries
                    .getPhotosForVisit(visitId)
                    .asFlow()
                    .mapToList(Dispatchers.IO)
                    .first()
                    .map { mapToPhoto(it) }
                    .also { photos ->
                        logger.debug { "Found ${photos.size} photos for visit $visitId" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to get photos for visit $visitId" }
                throw e
            }
        }

    override suspend fun isPhotoAttachedToVisit(
        photoId: String,
        visitId: String
    ): Boolean =
        withContext(Dispatchers.IO) {
            logger.trace { "Checking if photo $photoId is attached to visit $visitId" }
            try {
                queries.isPhotoAttachedToVisit(photoId, visitId).executeAsOne()
            } catch (e: Exception) {
                logger.error(e) { "Failed to check photo attachment" }
                throw e
            }
        }

    override suspend fun deleteAttachment(attachmentId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Deleting attachment $attachmentId" }
                queries.deleteAttachment(attachmentId)
                logger.info { "Attachment $attachmentId deleted" }
            }
        }

    override suspend fun deleteAttachmentsForPhoto(photoId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Deleting all attachments for photo $photoId" }
                queries.deleteAttachmentsForPhoto(photoId)
                logger.info { "Attachments for photo $photoId deleted" }
            }
        }

    override suspend fun deleteAttachmentsForVisit(visitId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Deleting all attachments for visit $visitId" }
                queries.deleteAttachmentsForVisit(visitId)
                logger.info { "Attachments for visit $visitId deleted" }
            }
        }

    /** Map database row to Photo domain object. */
    private fun mapToPhoto(row: com.po4yka.trailglass.db.Photos): Photo =
        Photo(
            id = row.id,
            uri = row.uri,
            timestamp = Instant.fromEpochMilliseconds(row.timestamp),
            latitude = row.latitude,
            longitude = row.longitude,
            width = row.width?.toInt(),
            height = row.height?.toInt(),
            sizeBytes = row.size_bytes,
            mimeType = row.mime_type,
            userId = row.user_id,
            addedAt = Instant.fromEpochMilliseconds(row.added_at)
        )

    private fun mapToPhoto(row: com.po4yka.trailglass.db.GetPhotosNearLocation): Photo =
        Photo(
            id = row.id,
            uri = row.uri,
            timestamp = Instant.fromEpochMilliseconds(row.timestamp),
            latitude = row.latitude,
            longitude = row.longitude,
            width = row.width?.toInt(),
            height = row.height?.toInt(),
            sizeBytes = row.size_bytes,
            mimeType = row.mime_type,
            userId = row.user_id,
            addedAt = Instant.fromEpochMilliseconds(row.added_at)
        )

    /** Map database row to PhotoAttachment domain object. */
    private fun mapToPhotoAttachment(row: com.po4yka.trailglass.db.Photo_attachments): PhotoAttachment =
        PhotoAttachment(
            id = row.id,
            photoId = row.photo_id,
            placeVisitId = row.place_visit_id,
            attachedAt = Instant.fromEpochMilliseconds(row.attached_at),
            caption = row.caption
        )
}
