package com.po4yka.trailglass.data.remote

/**
 * API endpoint constants for TrailGlass backend.
 * Provides centralized endpoint definitions for all API operations.
 */
internal object ApiEndpoints {
    // Authentication
    const val AUTH_REGISTER = "/auth/register"
    const val AUTH_LOGIN = "/auth/login"
    const val AUTH_LOGOUT = "/auth/logout"
    const val AUTH_REFRESH = "/auth/refresh"

    // Sync
    const val SYNC_STATUS = "/sync/status"
    const val SYNC_DELTA = "/sync/delta"
    const val SYNC_RESOLVE_CONFLICT = "/sync/resolve-conflict"

    // Locations
    const val LOCATIONS = "/locations"
    const val LOCATIONS_BATCH = "/locations/batch"

    fun locationById(id: String) = "/locations/$id"

    // Place Visits
    const val PLACE_VISITS = "/place-visits"

    fun placeVisitById(id: String) = "/place-visits/$id"

    // Trips
    const val TRIPS = "/trips"

    fun tripById(id: String) = "/trips/$id"

    // Settings
    const val SETTINGS = "/settings"

    // User Profile
    const val USER_PROFILE = "/user/profile"
    const val USER_DEVICES = "/user/devices"

    fun userDeviceById(deviceId: String) = "/user/devices/$deviceId"

    // Data Export
    const val EXPORT_REQUEST = "/export/request"

    fun exportStatus(exportId: String) = "/export/$exportId/status"
}
