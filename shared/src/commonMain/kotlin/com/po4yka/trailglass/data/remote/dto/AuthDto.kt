package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/** Device information for registration and authentication. */
@Serializable
data class DeviceInfoDto(
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String
)

/** User registration request. */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val deviceInfo: DeviceInfoDto
)

/** User registration response. */
@Serializable
data class RegisterResponse(
    val userId: String,
    val email: String,
    val displayName: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val createdAt: String
)

/** Login request. */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceInfo: DeviceInfoDto
)

/** Login response. */
@Serializable
data class LoginResponse(
    val userId: String,
    val email: String,
    val displayName: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val lastSyncTimestamp: String?
)

/** Token refresh request. */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/** Token refresh response. */
@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

/** Logout request. */
@Serializable
data class LogoutRequest(
    val deviceId: String
)
