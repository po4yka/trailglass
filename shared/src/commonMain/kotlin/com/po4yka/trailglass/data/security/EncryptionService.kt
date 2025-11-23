package com.po4yka.trailglass.data.security

/**
 * Service for end-to-end encryption of user data.
 *
 * Uses AES-256-GCM encryption for data confidentiality and integrity. Keys are stored securely on the device and never
 * sent to the server.
 *
 * Platform implementations:
 * - Android: Uses Android Keystore with hardware-backed keys
 * - iOS: Uses iOS Keychain with Secure Enclave
 */
expect class EncryptionService() {
    /**
     * Encrypt data using AES-256-GCM.
     *
     * @param plaintext The data to encrypt (UTF-8 string)
     * @return EncryptedData containing ciphertext, IV, and authentication tag
     */
    suspend fun encrypt(plaintext: String): Result<EncryptedData>

    /**
     * Decrypt data using AES-256-GCM.
     *
     * @param encryptedData The encrypted data with IV and tag
     * @return Decrypted plaintext string
     */
    suspend fun decrypt(encryptedData: EncryptedData): Result<String>

    /** Check if encryption key exists. */
    suspend fun hasEncryptionKey(): Boolean

    /** Generate a new encryption key. This will overwrite any existing key. */
    suspend fun generateKey(): Result<Unit>

    /**
     * Export the encryption key for backup. Returns a password-encrypted backup of the key.
     *
     * @param password User password to encrypt the key backup
     * @return Base64-encoded encrypted key backup
     */
    suspend fun exportKey(password: String): Result<String>

    /**
     * Import an encryption key from backup.
     *
     * @param encryptedKeyBackup Base64-encoded encrypted key backup
     * @param password User password to decrypt the key backup
     */
    suspend fun importKey(
        encryptedKeyBackup: String,
        password: String
    ): Result<Unit>

    /** Delete the encryption key. WARNING: This will make all encrypted data unrecoverable! */
    suspend fun deleteKey(): Result<Unit>
}

/** Encrypted data container with all components needed for decryption. */
data class EncryptedData(
    /** Base64-encoded ciphertext */
    val ciphertext: String,
    /** Base64-encoded initialization vector (12 bytes for GCM) */
    val iv: String,
    /** Base64-encoded authentication tag (16 bytes for GCM) */
    val tag: String
) {
    /** Combine into a single string for storage. Format: iv:tag:ciphertext */
    fun toStorageFormat(): String = "$iv:$tag:$ciphertext"

    companion object {
        /** Parse from storage format. */
        fun fromStorageFormat(data: String): EncryptedData? {
            val parts = data.split(":")
            if (parts.size != 3) return null
            return EncryptedData(
                ciphertext = parts[2],
                iv = parts[0],
                tag = parts[1]
            )
        }
    }
}

/** Exception thrown when encryption operations fail. */
class EncryptionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
