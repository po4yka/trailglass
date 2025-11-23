package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/** API error response. */
@Serializable
data class ApiErrorResponse(
    val error: String,
    val message: String,
    val details: Map<String, String>? = null,
    val timestamp: String,
    val requestId: String? = null
)

/** Common API error codes. */
object ApiErrorCode {
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val FORBIDDEN = "FORBIDDEN"
    const val NOT_FOUND = "NOT_FOUND"
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val VERSION_CONFLICT = "VERSION_CONFLICT"
    const val RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED"
    const val INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"
    const val SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
}
