package com.po4yka.trailglass.data.security

import com.po4yka.trailglass.logging.logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/**
 * Service for encrypting and decrypting sync data payloads.
 *
 * This service integrates with the sync system to provide end-to-end encryption
 * for user data. When E2E encryption is enabled:
 * - All sync data is encrypted on the client before sending to the server
 * - All received data is decrypted on the client after receiving from the server
 * - The server only stores encrypted blobs - cannot read the actual data
 *
 * The encryption key never leaves the device.
 */
@Inject
class SyncDataEncryption(
    private val encryptionService: EncryptionService,
    private val json: Json
) {
    private val logger = logger()

    /**
     * Encrypt sync payload for transmission to server.
     *
     * @param data The data object to encrypt
     * @param serializer The serializer for the data type
     * @return Encrypted payload wrapper containing the encrypted data
     */
    suspend fun <T> encryptSyncData(
        data: T,
        serializer: KSerializer<T>
    ): Result<EncryptedSyncPayload> =
        try {
            // Serialize to JSON
            val jsonString = json.encodeToString(serializer, data)

            logger.debug { "Encrypting sync data (${jsonString.length} bytes)" }

            // Encrypt the JSON
            val encryptedData = encryptionService.encrypt(jsonString).getOrThrow()

            Result.success(
                EncryptedSyncPayload(
                    encryptedData = encryptedData.toStorageFormat(),
                    encryptionVersion = ENCRYPTION_VERSION
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to encrypt sync data" }
            Result.failure(e)
        }

    /**
     * Decrypt sync payload received from server.
     *
     * @param payload The encrypted payload wrapper
     * @param deserializer The deserializer for the data type
     * @return Decrypted data object
     */
    suspend fun <T> decryptSyncData(
        payload: EncryptedSyncPayload,
        deserializer: KSerializer<T>
    ): Result<T> =
        try {
            if (payload.encryptionVersion != ENCRYPTION_VERSION) {
                throw EncryptionException(
                    "Unsupported encryption version: ${payload.encryptionVersion}"
                )
            }

            logger.debug { "Decrypting sync data (version ${payload.encryptionVersion})" }

            // Parse encrypted data
            val encryptedData =
                EncryptedData.fromStorageFormat(payload.encryptedData)
                    ?: throw EncryptionException("Invalid encrypted data format")

            // Decrypt
            val jsonString = encryptionService.decrypt(encryptedData).getOrThrow()

            // Deserialize
            val data = json.decodeFromString(deserializer, jsonString)

            Result.success(data)
        } catch (e: Exception) {
            logger.error(e) { "Failed to decrypt sync data" }
            Result.failure(e)
        }

    /**
     * Check if E2E encryption is available (key exists).
     */
    suspend fun isEncryptionAvailable(): Boolean = encryptionService.hasEncryptionKey()

    /**
     * Initialize E2E encryption by generating a key if needed.
     */
    suspend fun initializeEncryption(): Result<Unit> =
        if (!encryptionService.hasEncryptionKey()) {
            logger.info { "Generating new E2E encryption key" }
            encryptionService.generateKey()
        } else {
            logger.info { "E2E encryption key already exists" }
            Result.success(Unit)
        }

    /**
     * Export encryption key for backup (password-protected).
     */
    suspend fun exportEncryptionKey(password: String): Result<String> {
        logger.info { "Exporting E2E encryption key" }
        return encryptionService.exportKey(password)
    }

    /**
     * Import encryption key from backup (password-protected).
     */
    suspend fun importEncryptionKey(
        encryptedBackup: String,
        password: String
    ): Result<Unit> {
        logger.info { "Importing E2E encryption key" }
        return encryptionService.importKey(encryptedBackup, password)
    }

    /**
     * Delete encryption key.
     * WARNING: This will make all encrypted data permanently unrecoverable!
     */
    suspend fun deleteEncryptionKey(): Result<Unit> {
        logger.warn { "Deleting E2E encryption key - encrypted data will be unrecoverable!" }
        return encryptionService.deleteKey()
    }

    companion object {
        /**
         * Current encryption version.
         * Increment this if the encryption format changes.
         */
        private const val ENCRYPTION_VERSION = 1
    }
}

/**
 * Encrypted sync payload sent to/from server.
 * The server stores this as an opaque blob without access to the actual data.
 */
@Serializable
data class EncryptedSyncPayload(
    /**
     * Encrypted data in storage format (iv:tag:ciphertext).
     */
    val encryptedData: String,
    /**
     * Encryption version for forward compatibility.
     */
    val encryptionVersion: Int
)
