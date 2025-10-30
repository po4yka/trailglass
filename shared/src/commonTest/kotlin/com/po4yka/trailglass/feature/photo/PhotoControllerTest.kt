package com.po4yka.trailglass.feature.photo

import app.cash.turbine.test
import com.po4yka.trailglass.data.sync.createMockPlaceVisit
import com.po4yka.trailglass.domain.model.Photo
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test

class PhotoControllerTest {

    private val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val userId = "test_user"

    @Test
    fun `should initialize with empty state`() = runTest {
        val controller = createController()

        controller.state.test {
            val state = awaitItem()
            state.selectedDate shouldBe null
            state.photos shouldBe emptyList()
            state.suggestedPhotos shouldBe emptyList()
            state.selectedVisit shouldBe null
            state.isLoading shouldBe false
            state.error shouldBe null
        }
    }

    @Test
    fun `loadPhotosForDay should load photos`() = runTest {
        val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val mockPhotos = listOf(createMockPhoto("photo1"))
        val getPhotosUseCase = MockGetPhotosForDayUseCase(photos = mockPhotos)

        val controller = PhotoController(
            getPhotosForDayUseCase = getPhotosUseCase,
            suggestPhotosUseCase = MockSuggestPhotosUseCase(),
            attachPhotoUseCase = MockAttachPhotoUseCase(),
            permissionFlow = mockk(relaxed = true),
            coroutineScope = testScope,
            userId = userId
        )

        controller.state.test {
            awaitItem()

            controller.loadPhotosForDay(date)

            val loading = awaitItem()
            loading.isLoading shouldBe true
            loading.selectedDate shouldBe date

            val loaded = awaitItem()
            loaded.isLoading shouldBe false
            loaded.photos shouldBe mockPhotos
        }
    }

    @Test
    fun `loadPhotosForDay should handle errors`() = runTest {
        val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val getPhotosUseCase = MockGetPhotosForDayUseCase(shouldThrow = true)
        val controller = PhotoController(
            getPhotosForDayUseCase = getPhotosUseCase,
            suggestPhotosUseCase = MockSuggestPhotosUseCase(),
            attachPhotoUseCase = MockAttachPhotoUseCase(),
            permissionFlow = mockk(relaxed = true),
            coroutineScope = testScope,
            userId = userId
        )

        controller.state.test {
            awaitItem()

            controller.loadPhotosForDay(date)

            awaitItem() // Loading
            val error = awaitItem()
            error.isLoading shouldBe false
            error.error shouldBe "Test error"
        }
    }

    @Test
    fun `loadSuggestionsForVisit should load suggested photos`() = runTest {
        val visit = createMockPlaceVisit("visit1")
        val mockPhotos = listOf(createMockPhoto("photo1"))
        val suggestUseCase = MockSuggestPhotosUseCase(photos = mockPhotos)

        val controller = PhotoController(
            getPhotosForDayUseCase = MockGetPhotosForDayUseCase(),
            suggestPhotosUseCase = suggestUseCase,
            attachPhotoUseCase = MockAttachPhotoUseCase(),
            permissionFlow = mockk(relaxed = true),
            coroutineScope = testScope,
            userId = userId
        )

        controller.state.test {
            awaitItem()

            controller.loadSuggestionsForVisit(visit)

            val loading = awaitItem()
            loading.isLoading shouldBe true
            loading.selectedVisit shouldBe visit

            val loaded = awaitItem()
            loaded.isLoading shouldBe false
            loaded.suggestedPhotos shouldBe mockPhotos
        }
    }

    @Test
    fun `attachPhotoToVisit should succeed when visit selected`() = runTest {
        val visit = createMockPlaceVisit("visit1")
        val attachUseCase = MockAttachPhotoUseCase()

        val controller = PhotoController(
            getPhotosForDayUseCase = MockGetPhotosForDayUseCase(),
            suggestPhotosUseCase = MockSuggestPhotosUseCase(),
            attachPhotoUseCase = attachUseCase,
            permissionFlow = mockk(relaxed = true),
            coroutineScope = testScope,
            userId = userId
        )

        controller.state.test {
            awaitItem()

            // First select a visit
            controller.loadSuggestionsForVisit(visit)
            awaitItem() // Loading
            awaitItem() // Loaded

            // Now attach photo
            controller.attachPhotoToVisit("photo1", "Test caption")

            awaitItem() // Loading
            awaitItem() // Loaded
            awaitItem() // Refresh suggestions (loading)
            awaitItem() // Suggestions loaded

            attachUseCase.called shouldBe true
        }
    }

    @Test
    fun `attachPhotoToVisit should handle already attached error`() = runTest {
        val visit = createMockPlaceVisit("visit1")
        val attachUseCase = MockAttachPhotoUseCase(
            result = AttachPhotoToVisitUseCase.Result.AlreadyAttached
        )

        val controller = PhotoController(
            getPhotosForDayUseCase = MockGetPhotosForDayUseCase(),
            suggestPhotosUseCase = MockSuggestPhotosUseCase(),
            attachPhotoUseCase = attachUseCase,
            permissionFlow = mockk(relaxed = true),
            coroutineScope = testScope,
            userId = userId
        )

        controller.state.test {
            awaitItem()

            controller.loadSuggestionsForVisit(visit)
            awaitItem() // Loading
            awaitItem() // Loaded

            controller.attachPhotoToVisit("photo1")

            awaitItem() // Loading
            val error = awaitItem()
            error.isLoading shouldBe false
            error.error shouldBe "Photo already attached"
        }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        val controller = createController(
            getPhotosUseCase = MockGetPhotosForDayUseCase(shouldThrow = true)
        )

        controller.state.test {
            awaitItem()

            val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
            controller.loadPhotosForDay(date)
            awaitItem() // Loading
            awaitItem().error shouldBe "Test error"

            controller.clearError()

            val cleared = awaitItem()
            cleared.error shouldBe null
        }
    }

    private fun createController(
        getPhotosUseCase: GetPhotosForDayUseCase = MockGetPhotosForDayUseCase(),
        suggestPhotosUseCase: SuggestPhotosForVisitUseCase = MockSuggestPhotosUseCase(),
        attachPhotoUseCase: AttachPhotoToVisitUseCase = MockAttachPhotoUseCase()
    ): PhotoController {
        return PhotoController(
            getPhotosForDayUseCase = getPhotosUseCase,
            suggestPhotosUseCase = suggestPhotosUseCase,
            attachPhotoUseCase = attachPhotoUseCase,
            permissionFlow = mockk(relaxed = true),
            coroutineScope = testScope,
            userId = userId
        )
    }

    private fun createMockPhoto(id: String): Photo {
        return Photo(
            id = id,
            userId = userId,
            filePath = "/path/to/$id.jpg",
            timestamp = Clock.System.now(),
            latitude = 37.7749,
            longitude = -122.4194,
            thumbnailPath = "/path/to/thumb_$id.jpg"
        )
    }
}

class MockGetPhotosForDayUseCase(
    private val photos: List<Photo> = emptyList(),
    private val shouldThrow: Boolean = false
) : GetPhotosForDayUseCase {
    override suspend fun execute(date: LocalDate, userId: String): List<Photo> {
        if (shouldThrow) throw Exception("Test error")
        return photos
    }
}

class MockSuggestPhotosUseCase(
    private val photos: List<Photo> = emptyList(),
    private val shouldThrow: Boolean = false
) : SuggestPhotosForVisitUseCase {
    override suspend fun execute(
        visit: com.po4yka.trailglass.domain.model.PlaceVisit,
        userId: String
    ): List<Photo> {
        if (shouldThrow) throw Exception("Test error")
        return photos
    }
}

class MockAttachPhotoUseCase(
    private val result: AttachPhotoToVisitUseCase.Result = AttachPhotoToVisitUseCase.Result.Success
) : AttachPhotoToVisitUseCase {
    var called = false

    override suspend fun execute(
        photoId: String,
        visitId: String,
        caption: String?
    ): AttachPhotoToVisitUseCase.Result {
        called = true
        return result
    }
}
