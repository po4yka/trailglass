package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.Note
import kotlinx.datetime.Instant

/** Repository for managing user notes/journals. */
interface NoteRepository {
    /** Insert a new note. */
    suspend fun insertNote(note: Note)

    /** Get a note by ID. */
    suspend fun getNoteById(noteId: String): Note?

    /** Get all notes for a user. */
    suspend fun getNotesForUser(userId: String): List<Note>

    /** Get notes within a time range. */
    suspend fun getNotesInRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<Note>

    /** Update an existing note. */
    suspend fun updateNote(note: Note)

    /** Delete a note. */
    suspend fun deleteNote(noteId: String)
}
