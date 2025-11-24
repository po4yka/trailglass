package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Domain model for user notes/journals.
 */
@Serializable
data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val location: Coordinate? = null,
    val timestamp: Instant,
    val createdAt: Instant,
    val updatedAt: Instant
)
