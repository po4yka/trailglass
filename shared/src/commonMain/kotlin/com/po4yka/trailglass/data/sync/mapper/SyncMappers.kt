package com.po4yka.trailglass.data.sync.mapper

import com.po4yka.trailglass.data.remote.dto.*
import com.po4yka.trailglass.domain.model.*
import kotlinx.datetime.Instant

/**
 * Mappers for converting between domain models and sync DTOs.
 */

// ========== PlaceVisit Mappers ==========

fun PlaceVisit.toDto(
    syncAction: SyncAction = SyncAction.CREATE,
    localVersion: Long = 1,
    serverVersion: Long? = null,
    deviceId: String
): PlaceVisitDto {
    return PlaceVisitDto(
        id = id,
        location = CoordinateDto(
            latitude = centerLatitude,
            longitude = centerLongitude
        ),
        placeName = poiName ?: userLabel,
        address = approximateAddress,
        category = category.name,
        arrivalTime = startTime.toString(),
        departureTime = endTime.toString(),
        durationMinutes = ((endTime - startTime).inWholeMinutes).toInt(),
        confidence = when (categoryConfidence) {
            CategoryConfidence.HIGH -> 0.9
            CategoryConfidence.MEDIUM -> 0.7
            CategoryConfidence.LOW -> 0.5
        },
        isFavorite = isFavorite,
        notes = userNotes,
        photos = emptyList(), // TODO: Link photo IDs
        tripId = null, // TODO: Link trip ID if part of a trip
        syncAction = syncAction,
        localVersion = localVersion,
        serverVersion = serverVersion,
        lastModified = updatedAt?.toString() ?: createdAt?.toString(),
        deviceId = deviceId
    )
}

fun PlaceVisitDto.toDomain(): PlaceVisit {
    return PlaceVisit(
        id = id,
        startTime = Instant.parse(arrivalTime),
        endTime = departureTime?.let { Instant.parse(it) } ?: Instant.parse(arrivalTime),
        centerLatitude = location.latitude,
        centerLongitude = location.longitude,
        approximateAddress = address,
        poiName = placeName,
        city = null, // Not in DTO
        countryCode = null, // Not in DTO
        category = try { PlaceCategory.valueOf(category) } catch (e: Exception) { PlaceCategory.OTHER },
        categoryConfidence = when {
            confidence >= 0.8 -> CategoryConfidence.HIGH
            confidence >= 0.6 -> CategoryConfidence.MEDIUM
            else -> CategoryConfidence.LOW
        },
        significance = PlaceSignificance.RARE, // Default
        userLabel = if (placeName != null && placeName != address) placeName else null,
        userNotes = notes,
        isFavorite = isFavorite,
        frequentPlaceId = null,
        userId = createdBy,
        createdAt = lastModified?.let { Instant.parse(it) },
        updatedAt = lastModified?.let { Instant.parse(it) }
    )
}

// ========== Trip Mappers ==========

fun Trip.toDto(
    syncAction: SyncAction = SyncAction.CREATE,
    localVersion: Long = 1,
    serverVersion: Long? = null,
    deviceId: String
): TripDto {
    return TripDto(
        id = id,
        name = name,
        startDate = startTime.toString(),
        endDate = endTime.toString(),
        placeVisits = visitIds,
        photos = photoIds,
        notes = notes,
        totalDistance = null, // TODO: Calculate from route segments
        countries = emptyList(), // TODO: Extract from place visits
        syncAction = syncAction,
        localVersion = localVersion,
        serverVersion = serverVersion,
        lastModified = updatedAt?.toString() ?: createdAt?.toString(),
        deviceId = deviceId
    )
}

fun TripDto.toDomain(): Trip {
    return Trip(
        id = id,
        name = name,
        startTime = Instant.parse(startDate),
        endTime = Instant.parse(endDate),
        visitIds = placeVisits,
        photoIds = photos,
        notes = notes,
        routeSegmentIds = emptyList(), // Not in DTO
        createdAt = lastModified?.let { Instant.parse(it) },
        updatedAt = lastModified?.let { Instant.parse(it) },
        userId = createdBy
    )
}

// ========== Photo Mappers ==========

fun Photo.toMetadataDto(
    syncAction: SyncAction = SyncAction.CREATE,
    localVersion: Long = 1,
    serverVersion: Long? = null,
    deviceId: String
): PhotoMetadataDto {
    return PhotoMetadataDto(
        id = id,
        timestamp = timestamp.toString(),
        location = location?.let { CoordinateDto(it.latitude, it.longitude) },
        placeVisitId = placeVisitId,
        tripId = tripId,
        caption = caption,
        url = null, // Server will provide URL after upload
        thumbnailUrl = null,
        exifData = exifData?.let {
            ExifDataDto(
                cameraModel = it.cameraModel,
                focalLength = it.focalLength,
                aperture = it.aperture,
                iso = it.iso,
                shutterSpeed = it.shutterSpeed
            )
        },
        syncAction = syncAction,
        localVersion = localVersion,
        serverVersion = serverVersion,
        lastModified = timestamp.toString(),
        deviceId = deviceId
    )
}

fun PhotoMetadataDto.toDomain(localPath: String): Photo {
    return Photo(
        id = id,
        filePath = localPath,
        timestamp = Instant.parse(timestamp),
        location = location?.let { Coordinate(it.latitude, it.longitude) },
        placeVisitId = placeVisitId,
        tripId = tripId,
        caption = caption,
        exifData = exifData?.let {
            ExifData(
                cameraModel = it.cameraModel,
                focalLength = it.focalLength,
                aperture = it.aperture,
                iso = it.iso,
                shutterSpeed = it.shutterSpeed
            )
        },
        uploadedAt = lastModified?.let { Instant.parse(it) },
        userId = deviceId
    )
}

// ========== Settings Mappers ==========

fun SettingsDto.toDomain(): Map<String, Any> {
    // Convert DTO to a map for flexible settings handling
    val settingsMap = mutableMapOf<String, Any>()

    trackingPreferences?.let { tracking ->
        settingsMap["tracking_accuracy"] = tracking.accuracy
        settingsMap["tracking_interval"] = tracking.updateInterval
        settingsMap["tracking_battery_optimization"] = tracking.batteryOptimization
        settingsMap["tracking_when_stationary"] = tracking.trackWhenStationary
        settingsMap["tracking_minimum_distance"] = tracking.minimumDistance
    }

    privacySettings?.let { privacy ->
        settingsMap["privacy_retention_days"] = privacy.dataRetentionDays
        settingsMap["privacy_analytics"] = privacy.allowAnonymousAnalytics
        settingsMap["privacy_share_location"] = privacy.shareLocation
        settingsMap["privacy_require_auth"] = privacy.requireAuthentication
        settingsMap["privacy_auto_backup"] = privacy.autoBackup
    }

    unitPreferences?.let { units ->
        settingsMap["units_distance"] = units.distanceUnit
        settingsMap["units_temperature"] = units.temperatureUnit
        settingsMap["units_time_format"] = units.timeFormat
        settingsMap["units_first_day"] = units.firstDayOfWeek
    }

    appearanceSettings?.let { appearance ->
        settingsMap["appearance_theme"] = appearance.theme
        settingsMap["appearance_accent"] = appearance.accentColor
        settingsMap["appearance_map_style"] = appearance.mapStyle
        settingsMap["appearance_animations"] = appearance.enableAnimations
    }

    return settingsMap
}
