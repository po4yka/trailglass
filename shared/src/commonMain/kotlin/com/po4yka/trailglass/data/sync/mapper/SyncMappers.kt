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

// ========== Location Mappers ==========

fun LocationSample.toDto(
    syncAction: SyncAction = SyncAction.CREATE,
    localVersion: Long = 1,
    serverVersion: Long? = null,
    deviceId: String
): LocationDto {
    return LocationDto(
        id = id,
        timestamp = timestamp.toString(),
        latitude = latitude,
        longitude = longitude,
        altitude = null,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        provider = source.name,
        batteryLevel = null,
        clientTimestamp = timestamp.toString(),
        syncAction = syncAction,
        localVersion = localVersion,
        serverVersion = serverVersion,
        deviceId = deviceId
    )
}

fun LocationDto.toDomain(userId: String): LocationSample {
    return LocationSample(
        id = id,
        timestamp = Instant.parse(timestamp),
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        source = try { LocationSource.valueOf(provider) } catch (e: Exception) { LocationSource.GPS },
        tripId = null,
        uploadedAt = serverVersion?.let { Instant.parse(timestamp) },
        deviceId = deviceId ?: "unknown",
        userId = userId
    )
}

// ========== Settings Mappers ==========

fun AppSettings.toDto(
    serverVersion: Long? = null,
    lastModified: Instant
): SettingsDto {
    return SettingsDto(
        trackingPreferences = TrackingPreferencesDto(
            accuracy = trackingPreferences.accuracy.name,
            updateInterval = trackingPreferences.updateInterval.name,
            batteryOptimization = trackingPreferences.batteryOptimization,
            trackWhenStationary = trackingPreferences.trackWhenStationary,
            minimumDistance = trackingPreferences.minimumDistance
        ),
        privacySettings = PrivacySettingsDto(
            dataRetentionDays = privacySettings.dataRetentionDays,
            allowAnonymousAnalytics = privacySettings.shareAnalytics,
            shareLocation = accountSettings.autoSync,
            requireAuthentication = true,
            autoBackup = privacySettings.autoBackup
        ),
        unitPreferences = UnitPreferencesDto(
            distanceUnit = unitPreferences.distanceUnit.name,
            temperatureUnit = unitPreferences.temperatureUnit.name,
            timeFormat = unitPreferences.timeFormat.name,
            firstDayOfWeek = "MONDAY" // Default
        ),
        appearanceSettings = AppearanceSettingsDto(
            theme = appearanceSettings.theme.name,
            accentColor = "#6200EE", // Default material purple
            mapStyle = "STANDARD",
            enableAnimations = !appearanceSettings.compactView
        ),
        serverVersion = serverVersion,
        lastModified = lastModified.toString()
    )
}

fun SettingsDto.toDomain(): AppSettings {
    return AppSettings(
        trackingPreferences = TrackingPreferences(
            accuracy = trackingPreferences?.let {
                try { TrackingAccuracy.valueOf(it.accuracy) }
                catch (e: Exception) { TrackingAccuracy.BALANCED }
            } ?: TrackingAccuracy.BALANCED,
            updateInterval = trackingPreferences?.let {
                try { UpdateInterval.valueOf(it.updateInterval) }
                catch (e: Exception) { UpdateInterval.NORMAL }
            } ?: UpdateInterval.NORMAL,
            batteryOptimization = trackingPreferences?.batteryOptimization ?: true,
            trackWhenStationary = trackingPreferences?.trackWhenStationary ?: false,
            minimumDistance = trackingPreferences?.minimumDistance ?: 10
        ),
        privacySettings = PrivacySettings(
            dataRetentionDays = privacySettings?.dataRetentionDays ?: 365,
            shareAnalytics = privacySettings?.allowAnonymousAnalytics ?: false,
            shareCrashReports = true,
            autoBackup = privacySettings?.autoBackup ?: true,
            encryptBackups = true
        ),
        unitPreferences = UnitPreferences(
            distanceUnit = unitPreferences?.let {
                try { DistanceUnit.valueOf(it.distanceUnit) }
                catch (e: Exception) { DistanceUnit.METRIC }
            } ?: DistanceUnit.METRIC,
            temperatureUnit = unitPreferences?.let {
                try { TemperatureUnit.valueOf(it.temperatureUnit) }
                catch (e: Exception) { TemperatureUnit.CELSIUS }
            } ?: TemperatureUnit.CELSIUS,
            timeFormat = unitPreferences?.let {
                try { TimeFormat.valueOf(it.timeFormat) }
                catch (e: Exception) { TimeFormat.TWENTY_FOUR_HOUR }
            } ?: TimeFormat.TWENTY_FOUR_HOUR
        ),
        appearanceSettings = AppearanceSettings(
            theme = appearanceSettings?.let {
                try { AppTheme.valueOf(it.theme) }
                catch (e: Exception) { AppTheme.SYSTEM }
            } ?: AppTheme.SYSTEM,
            useDeviceWallpaper = false,
            showMapInTimeline = true,
            compactView = appearanceSettings?.enableAnimations?.not() ?: false
        ),
        accountSettings = AccountSettings(
            email = null,
            autoSync = privacySettings?.shareLocation ?: true,
            syncOnWifiOnly = true,
            lastSyncTime = lastModified?.let { Instant.parse(it) }
        ),
        dataManagement = DataManagement()
    )
}
