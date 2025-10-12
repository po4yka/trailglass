package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.TestDatabaseHelper
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PhotoAttachment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.*

/**
 * Integration tests for PhotoRepository.
 */
class PhotoRepositoryTest {

    private lateinit var repository: PhotoRepository
    private val database = TestDatabaseHelper.createTestDatabase()
    private val userId = "test_user"

    @BeforeTest
    fun setup() {
        TestDatabaseHelper.clearDatabase(database)
        repository = PhotoRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseHelper.clearDatabase(database)
    }

    @Test
    fun testInsertAndGetPhoto() = runTest {
        // Given
        val photo = createTestPhoto(id = "photo1")

        // When
        repository.insertPhoto(photo)
        val result = repository.getPhotoById(photo.id, userId)

        // Then
        assertNotNull(result)
        assertEquals(photo.id, result.id)
        assertEquals(photo.uri, result.uri)
        assertEquals(photo.timestamp, result.timestamp)
        assertEquals(photo.latitude, result.latitude)
        assertEquals(photo.longitude, result.longitude)
    }

    @Test
    fun testGetPhotosForDay() = runTest {
        // Given
        val date = LocalDate(2024, 1, 15)
        val timezone = TimeZone.UTC
        val dayStart = date.atStartOfDayIn(timezone)

        val photo1 = createTestPhoto(
            id = "photo1",
            timestamp = Instant.parse("2024-01-15T10:00:00Z")
        )
        val photo2 = createTestPhoto(
            id = "photo2",
            timestamp = Instant.parse("2024-01-15T14:00:00Z")
        )
        val photo3 = createTestPhoto(
            id = "photo3",
            timestamp = Instant.parse("2024-01-16T10:00:00Z")
        )

        repository.insertPhoto(photo1)
        repository.insertPhoto(photo2)
        repository.insertPhoto(photo3)

        // When
        val results = repository.getPhotosForDay(userId, date, timezone).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "photo1" })
        assertTrue(results.any { it.id == "photo2" })
        assertFalse(results.any { it.id == "photo3" })
    }

    @Test
    fun testGetPhotosForTimeRange() = runTest {
        // Given
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T23:59:59Z")

        val photo1 = createTestPhoto(
            id = "photo1",
            timestamp = Instant.parse("2024-01-10T10:00:00Z")
        )
        val photo2 = createTestPhoto(
            id = "photo2",
            timestamp = Instant.parse("2024-01-20T14:00:00Z")
        )
        val photo3 = createTestPhoto(
            id = "photo3",
            timestamp = Instant.parse("2024-02-10T10:00:00Z")
        )

        repository.insertPhoto(photo1)
        repository.insertPhoto(photo2)
        repository.insertPhoto(photo3)

        // When
        val results = repository.getPhotosForTimeRange(userId, start, end).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "photo1" })
        assertTrue(results.any { it.id == "photo2" })
        assertFalse(results.any { it.id == "photo3" })
    }

    @Test
    fun testGetPhotosNearLocation() = runTest {
        // Given
        val centerLat = 48.8566
        val centerLon = 2.3522
        val radiusMeters = 1000.0

        val photo1 = createTestPhoto(
            id = "photo1",
            latitude = 48.8566,
            longitude = 2.3522
        )
        val photo2 = createTestPhoto(
            id = "photo2",
            latitude = 48.8606,
            longitude = 2.3376
        )
        val photo3 = createTestPhoto(
            id = "photo3",
            latitude = 51.5074,
            longitude = -0.1278
        )

        repository.insertPhoto(photo1)
        repository.insertPhoto(photo2)
        repository.insertPhoto(photo3)

        // When
        val results = repository.getPhotosNearLocation(
            userId,
            centerLat,
            centerLon,
            radiusMeters
        ).first()

        // Then
        assertTrue(results.size >= 1)
        assertTrue(results.any { it.id == "photo1" })
        assertFalse(results.any { it.id == "photo3" })
    }

    @Test
    fun testAttachPhotoToVisit() = runTest {
        // Given
        val photo = createTestPhoto(id = "photo1")
        val visitId = "visit1"
        val caption = "Beautiful view"

        repository.insertPhoto(photo)

        // When
        val attachment = PhotoAttachment(
            id = "attachment1",
            photoId = photo.id,
            placeVisitId = visitId,
            caption = caption
        )
        repository.attachPhotoToVisit(attachment)

        val result = repository.getAttachment(photo.id, visitId)

        // Then
        assertNotNull(result)
        assertEquals(photo.id, result.photoId)
        assertEquals(visitId, result.placeVisitId)
        assertEquals(caption, result.caption)
    }

    @Test
    fun testGetPhotosForVisit() = runTest {
        // Given
        val visitId = "visit1"
        val photo1 = createTestPhoto(id = "photo1")
        val photo2 = createTestPhoto(id = "photo2")
        val photo3 = createTestPhoto(id = "photo3")

        repository.insertPhoto(photo1)
        repository.insertPhoto(photo2)
        repository.insertPhoto(photo3)

        repository.attachPhotoToVisit(
            PhotoAttachment("att1", photo1.id, visitId, null)
        )
        repository.attachPhotoToVisit(
            PhotoAttachment("att2", photo2.id, visitId, "Caption")
        )
        repository.attachPhotoToVisit(
            PhotoAttachment("att3", photo3.id, "visit2", null)
        )

        // When
        val results = repository.getPhotosForVisit(visitId, userId).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "photo1" })
        assertTrue(results.any { it.id == "photo2" })
        assertFalse(results.any { it.id == "photo3" })
    }

    @Test
    fun testIsPhotoAttachedToVisit() = runTest {
        // Given
        val photo = createTestPhoto(id = "photo1")
        val visitId = "visit1"

        repository.insertPhoto(photo)

        // When - not attached
        val notAttached = repository.isPhotoAttachedToVisit(photo.id, visitId)

        // Then
        assertFalse(notAttached)

        // When - attach and check
        repository.attachPhotoToVisit(
            PhotoAttachment("att1", photo.id, visitId, null)
        )
        val attached = repository.isPhotoAttachedToVisit(photo.id, visitId)

        // Then
        assertTrue(attached)
    }

    @Test
    fun testDetachPhotoFromVisit() = runTest {
        // Given
        val photo = createTestPhoto(id = "photo1")
        val visitId = "visit1"

        repository.insertPhoto(photo)
        repository.attachPhotoToVisit(
            PhotoAttachment("att1", photo.id, visitId, null)
        )

        // When
        repository.detachPhotoFromVisit(photo.id, visitId)
        val result = repository.getAttachment(photo.id, visitId)

        // Then
        assertNull(result)
    }

    @Test
    fun testDeletePhoto() = runTest {
        // Given
        val photo = createTestPhoto(id = "photo1")
        val visitId = "visit1"

        repository.insertPhoto(photo)
        repository.attachPhotoToVisit(
            PhotoAttachment("att1", photo.id, visitId, null)
        )

        // When
        repository.deletePhoto(photo.id, userId)

        // Then
        val photoResult = repository.getPhotoById(photo.id, userId)
        val attachmentResult = repository.getAttachment(photo.id, visitId)

        assertNull(photoResult)
        assertNull(attachmentResult) // Should cascade delete
    }

    @Test
    fun testGetUnattachedPhotos() = runTest {
        // Given
        val photo1 = createTestPhoto(id = "photo1")
        val photo2 = createTestPhoto(id = "photo2")
        val photo3 = createTestPhoto(id = "photo3")

        repository.insertPhoto(photo1)
        repository.insertPhoto(photo2)
        repository.insertPhoto(photo3)

        repository.attachPhotoToVisit(
            PhotoAttachment("att1", photo1.id, "visit1", null)
        )

        // When
        val results = repository.getUnattachedPhotos(userId).first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "photo2" })
        assertTrue(results.any { it.id == "photo3" })
        assertFalse(results.any { it.id == "photo1" })
    }

    private fun createTestPhoto(
        id: String,
        uri: String = "content://media/images/$id",
        timestamp: Instant = Instant.parse("2024-01-15T12:00:00Z"),
        latitude: Double? = 48.8566,
        longitude: Double? = 2.3522,
        width: Int? = 1920,
        height: Int? = 1080
    ) = Photo(
        id = id,
        uri = uri,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        width = width,
        height = height,
        sizeBytes = 1024000L,
        mimeType = "image/jpeg",
        userId = userId
    )
}
