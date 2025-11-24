package com.po4yka.trailglass.feature.note

import com.po4yka.trailglass.data.repository.NoteRepository
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.Note
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.util.UuidGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Use case for adding a new note.
 */
@Inject
class AddNoteUseCase(
    private val noteRepository: NoteRepository
) {
    private val logger = logger()

    /**
     * Create and save a new note.
     *
     * @param userId User ID
     * @param title Note title
     * @param content Note content
     * @param location Optional location
     * @param timestamp Optional timestamp (defaults to now)
     * @return Result with created Note or error
     */
    suspend fun execute(
        userId: String,
        title: String,
        content: String,
        location: Coordinate? = null,
        timestamp: Instant = Clock.System.now()
    ): Result<Note> {
        return try {
            logger.info { "Creating note for user $userId" }

            val noteId = UuidGenerator.randomUUID()
            val now = Clock.System.now()

            val note = Note(
                id = noteId,
                userId = userId,
                title = title,
                content = content,
                location = location,
                timestamp = timestamp,
                createdAt = now,
                updatedAt = now
            )

            noteRepository.insertNote(note)

            logger.info { "Created note $noteId for user $userId" }

            Result.success(note)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create note for user $userId" }
            Result.failure(e)
        }
    }
}
