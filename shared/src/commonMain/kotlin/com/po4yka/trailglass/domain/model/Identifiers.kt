package com.po4yka.trailglass.domain.model

import kotlin.jvm.JvmInline

/**
 * Type-safe identifiers for domain entities.
 *
 * Value classes provide compile-time type safety without runtime overhead.
 * They prevent accidentally passing one type of ID where another is expected.
 *
 * Example usage:
 * ```
 * fun getTrip(tripId: TripId): Trip
 * fun getUserTrips(userId: UserId): List<Trip>
 *
 * // Compile error: Type mismatch
 * // getTrip(userId)
 * ```
 *
 * Migration note: These value classes can be adopted incrementally.
 * Use the type aliases below for gradual migration from raw String IDs.
 */

/** Unique identifier for a Trip. */
@JvmInline
value class TripId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a User. */
@JvmInline
value class UserId(val value: String) {
    override fun toString(): String = value

    companion object {
        /** Guest user ID for users who haven't created an account. */
        val GUEST = UserId("guest_user")
    }
}

/** Unique identifier for a LocationSample. */
@JvmInline
value class LocationSampleId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a PlaceVisit. */
@JvmInline
value class PlaceVisitId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a FrequentPlace. */
@JvmInline
value class FrequentPlaceId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a RouteSegment. */
@JvmInline
value class RouteSegmentId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a Photo. */
@JvmInline
value class PhotoId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a PhotoAttachment. */
@JvmInline
value class PhotoAttachmentId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a Region (geofence). */
@JvmInline
value class RegionId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a Note. */
@JvmInline
value class NoteId(val value: String) {
    override fun toString(): String = value
}

/** Unique identifier for a Device. */
@JvmInline
value class DeviceId(val value: String) {
    override fun toString(): String = value
}

/**
 * Extension functions for converting between String and typed IDs.
 *
 * These make it easy to work with existing String-based APIs.
 */

fun String.toTripId(): TripId = TripId(this)
fun String.toUserId(): UserId = UserId(this)
fun String.toLocationSampleId(): LocationSampleId = LocationSampleId(this)
fun String.toPlaceVisitId(): PlaceVisitId = PlaceVisitId(this)
fun String.toFrequentPlaceId(): FrequentPlaceId = FrequentPlaceId(this)
fun String.toRouteSegmentId(): RouteSegmentId = RouteSegmentId(this)
fun String.toPhotoId(): PhotoId = PhotoId(this)
fun String.toPhotoAttachmentId(): PhotoAttachmentId = PhotoAttachmentId(this)
fun String.toRegionId(): RegionId = RegionId(this)
fun String.toNoteId(): NoteId = NoteId(this)
fun String.toDeviceId(): DeviceId = DeviceId(this)

/**
 * Generate a random ID using Kotlin's UUID.
 *
 * Example:
 * ```
 * val tripId = randomTripId()
 * val userId = randomUserId()
 * ```
 */
@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomTripId(): TripId = TripId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomUserId(): UserId = UserId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomLocationSampleId(): LocationSampleId = LocationSampleId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomPlaceVisitId(): PlaceVisitId = PlaceVisitId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomFrequentPlaceId(): FrequentPlaceId = FrequentPlaceId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomRouteSegmentId(): RouteSegmentId = RouteSegmentId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomPhotoId(): PhotoId = PhotoId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomPhotoAttachmentId(): PhotoAttachmentId = PhotoAttachmentId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomRegionId(): RegionId = RegionId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomNoteId(): NoteId = NoteId(kotlin.uuid.Uuid.random().toString())

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
fun randomDeviceId(): DeviceId = DeviceId(kotlin.uuid.Uuid.random().toString())
