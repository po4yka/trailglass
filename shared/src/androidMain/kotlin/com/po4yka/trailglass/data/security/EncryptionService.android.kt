package com.po4yka.trailglass.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Android implementation of EncryptionService using Android Keystore.
 *
 * Uses AES-256-GCM with Android Keystore for hardware-backed key storage.
 */
@Inject
actual class EncryptionService actual constructor() {
    private val keyStore: KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

    actual suspend fun encrypt(plaintext: String): Result<EncryptedData> =
        withContext(Dispatchers.IO) {
            runCatching {
                val key = getOrCreateKey()
                val cipher = Cipher.getInstance(TRANSFORMATION)

                // Generate random IV (12 bytes for GCM)
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val iv = cipher.iv

                // Encrypt
                val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

                // GCM mode appends authentication tag to ciphertext
                // Extract the tag (last 16 bytes)
                val ciphertextWithoutTag = ciphertext.copyOfRange(0, ciphertext.size - TAG_LENGTH_BYTES)
                val tag = ciphertext.copyOfRange(ciphertext.size - TAG_LENGTH_BYTES, ciphertext.size)

                EncryptedData(
                    ciphertext = Base64.encodeToString(ciphertextWithoutTag, Base64.NO_WRAP),
                    iv = Base64.encodeToString(iv, Base64.NO_WRAP),
                    tag = Base64.encodeToString(tag, Base64.NO_WRAP)
                )
            }.onFailure { e ->
                Result.failure<EncryptedData>(EncryptionException("Encryption failed", e))
            }
        }

    actual suspend fun decrypt(encryptedData: EncryptedData): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val key = getOrCreateKey()
                val cipher = Cipher.getInstance(TRANSFORMATION)

                // Decode components
                val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)
                val ciphertext = Base64.decode(encryptedData.ciphertext, Base64.NO_WRAP)
                val tag = Base64.decode(encryptedData.tag, Base64.NO_WRAP)

                // Combine ciphertext and tag for GCM
                val ciphertextWithTag = ciphertext + tag

                // Decrypt
                val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, spec)
                val plaintext = cipher.doFinal(ciphertextWithTag)

                String(plaintext, Charsets.UTF_8)
            }.onFailure { e ->
                Result.failure<String>(EncryptionException("Decryption failed", e))
            }
        }

    actual suspend fun hasEncryptionKey(): Boolean =
        withContext(Dispatchers.IO) {
            keyStore.containsAlias(KEY_ALIAS)
        }

    actual suspend fun generateKey(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val keyGenerator =
                    KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        ANDROID_KEYSTORE
                    )

                val keyGenParameterSpec =
                    KeyGenParameterSpec
                        .Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setUserAuthenticationRequired(false) // Can be enabled for extra security
                        .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
                Unit
            }.onFailure { e ->
                Result.failure<Unit>(EncryptionException("Key generation failed", e))
            }
        }

    actual suspend fun exportKey(password: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Get the key from keystore
                val secretKey =
                    keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                        ?: throw EncryptionException("Encryption key not found")

                val keyBytes = secretKey.encoded

                // Derive a key from the password using PBKDF2
                val salt =
                    ByteArray(32).apply {
                        java.security.SecureRandom().nextBytes(this)
                    }

                val pbkdf2Key = deriveKeyFromPassword(password, salt)

                // Encrypt the key bytes with the derived key
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, pbkdf2Key)
                val iv = cipher.iv
                val encryptedKey = cipher.doFinal(keyBytes)

                // Combine: salt:iv:encryptedKey (all base64)
                val backup =
                    "${Base64.encodeToString(salt, Base64.NO_WRAP)}:" +
                        "${Base64.encodeToString(iv, Base64.NO_WRAP)}:" +
                        "${Base64.encodeToString(encryptedKey, Base64.NO_WRAP)}"

                backup
            }.onFailure { e ->
                Result.failure<String>(EncryptionException("Key export failed", e))
            }
        }

    actual suspend fun importKey(
        encryptedKeyBackup: String,
        password: String
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Parse backup
                val parts = encryptedKeyBackup.split(":")
                if (parts.size != 3) {
                    throw EncryptionException("Invalid key backup format")
                }

                val salt = Base64.decode(parts[0], Base64.NO_WRAP)
                val iv = Base64.decode(parts[1], Base64.NO_WRAP)
                val encryptedKey = Base64.decode(parts[2], Base64.NO_WRAP)

                // Derive key from password
                val pbkdf2Key = deriveKeyFromPassword(password, salt)

                // Decrypt the key
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
                cipher.init(Cipher.DECRYPT_MODE, pbkdf2Key, spec)
                val keyBytes = cipher.doFinal(encryptedKey)

                // Delete existing key if any
                if (keyStore.containsAlias(KEY_ALIAS)) {
                    keyStore.deleteEntry(KEY_ALIAS)
                }

                // Store the imported key
                val secretKey = SecretKeySpec(keyBytes, "AES")
                keyStore.setEntry(
                    KEY_ALIAS,
                    KeyStore.SecretKeyEntry(secretKey),
                    null
                )
            }.onFailure { e ->
                Result.failure<Unit>(EncryptionException("Key import failed", e))
            }
        }

    actual suspend fun deleteKey(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (keyStore.containsAlias(KEY_ALIAS)) {
                    keyStore.deleteEntry(KEY_ALIAS)
                }
            }.onFailure { e ->
                Result.failure<Unit>(EncryptionException("Key deletion failed", e))
            }
        }

    private fun getOrCreateKey(): SecretKey =
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            // Generate key synchronously if it doesn't exist
            val keyGenerator =
                KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )

            val keyGenParameterSpec =
                KeyGenParameterSpec
                    .Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

    private fun deriveKeyFromPassword(
        password: String,
        salt: ByteArray
    ): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "trailglass_e2e_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BITS = 128
        private const val TAG_LENGTH_BYTES = TAG_LENGTH_BITS / 8
        private const val PBKDF2_ITERATIONS = 100_000
    }
}
