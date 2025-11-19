package com.po4yka.trailglass.data.security

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import platform.CoreCrypto.*
import platform.Foundation.*
import platform.Security.*

/**
 * iOS implementation of EncryptionService using iOS Keychain and CommonCrypto.
 *
 * Uses AES-256-GCM with iOS Keychain for secure key storage.
 */
@Inject
@OptIn(ExperimentalForeignApi::class)
actual class EncryptionService actual constructor() {

    actual suspend fun encrypt(plaintext: String): Result<EncryptedData> = withContext(Dispatchers.IO) {
        runCatching {
            val key = getOrCreateKey()

            // Generate random IV (12 bytes for GCM)
            val iv = ByteArray(IV_LENGTH)
            SecRandomCopyBytes(kSecRandomDefault, IV_LENGTH.toULong(), iv.refTo(0))

            // Convert plaintext to bytes
            val plaintextData = plaintext.encodeToByteArray()

            // Encrypt using AES-GCM
            val ciphertextBuffer = ByteArray(plaintextData.size + TAG_LENGTH)
            val tagBuffer = ByteArray(TAG_LENGTH)

            plaintextData.usePinned { plaintextPin ->
                key.usePinned { keyPin ->
                    iv.usePinned { ivPin ->
                        ciphertextBuffer.usePinned { ciphertextPin ->
                            tagBuffer.usePinned { tagPin ->
                                val status = CCCryptorGCMOneshotEncrypt(
                                    kCCAlgorithmAES,
                                    keyPin.addressOf(0),
                                    key.size.toULong(),
                                    ivPin.addressOf(0),
                                    IV_LENGTH.toULong(),
                                    null, // aad
                                    0u, // aadLen
                                    plaintextPin.addressOf(0),
                                    plaintextData.size.toULong(),
                                    ciphertextPin.addressOf(0),
                                    tagPin.addressOf(0),
                                    tagPin.get().size.toULong()
                                )

                                if (status != kCCSuccess) {
                                    throw EncryptionException("Encryption failed with status: $status")
                                }
                            }
                        }
                    }
                }
            }

            val ciphertext = ciphertextBuffer.copyOfRange(0, plaintextData.size)

            EncryptedData(
                ciphertext = ciphertext.toNSData().base64EncodedStringWithOptions(0),
                iv = iv.toNSData().base64EncodedStringWithOptions(0),
                tag = tagBuffer.toNSData().base64EncodedStringWithOptions(0)
            )
        }.onFailure { e ->
            Result.failure<EncryptedData>(EncryptionException("Encryption failed", e))
        }
    }

    actual suspend fun decrypt(encryptedData: EncryptedData): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val key = getOrCreateKey()

            // Decode components
            val iv = encryptedData.iv.fromBase64()
            val ciphertext = encryptedData.ciphertext.fromBase64()
            val tag = encryptedData.tag.fromBase64()

            // Decrypt using AES-GCM
            val plaintextBuffer = ByteArray(ciphertext.size)

            ciphertext.usePinned { ciphertextPin ->
                key.usePinned { keyPin ->
                    iv.usePinned { ivPin ->
                        tag.usePinned { tagPin ->
                            plaintextBuffer.usePinned { plaintextPin ->
                                val status = CCCryptorGCMOneshotDecrypt(
                                    kCCAlgorithmAES,
                                    keyPin.addressOf(0),
                                    key.size.toULong(),
                                    ivPin.addressOf(0),
                                    IV_LENGTH.toULong(),
                                    null, // aad
                                    0u, // aadLen
                                    ciphertextPin.addressOf(0),
                                    ciphertext.size.toULong(),
                                    plaintextPin.addressOf(0),
                                    tagPin.addressOf(0),
                                    TAG_LENGTH.toULong()
                                )

                                if (status != kCCSuccess) {
                                    throw EncryptionException("Decryption failed with status: $status")
                                }
                            }
                        }
                    }
                }
            }

            plaintextBuffer.decodeToString()
        }.onFailure { e ->
            Result.failure<String>(EncryptionException("Decryption failed", e))
        }
    }

    actual suspend fun hasEncryptionKey(): Boolean = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = KEY_ACCOUNT
        query[kSecAttrService] = KEY_SERVICE
        query[kSecReturnData] = kCFBooleanFalse

        val status = SecItemCopyMatching(query as CFDictionaryRef, null)
        status == errSecSuccess
    }

    actual suspend fun generateKey(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Generate random 256-bit key
            val key = ByteArray(KEY_SIZE)
            SecRandomCopyBytes(kSecRandomDefault, KEY_SIZE.toULong(), key.refTo(0))

            // Delete existing key if any
            deleteKeyInternal()

            // Store in Keychain
            val query = mutableMapOf<Any?, Any?>()
            query[kSecClass] = kSecClassGenericPassword
            query[kSecAttrAccount] = KEY_ACCOUNT
            query[kSecAttrService] = KEY_SERVICE
            query[kSecValueData] = key.toNSData()
            query[kSecAttrAccessible] = kSecAttrAccessibleAfterFirstUnlock

            val status = SecItemAdd(query as CFDictionaryRef, null)
            if (status != errSecSuccess && status != errSecDuplicateItem) {
                throw EncryptionException("Failed to store key in Keychain: $status")
            }
        }.onFailure { e ->
            Result.failure<Unit>(EncryptionException("Key generation failed", e))
        }
    }

    actual suspend fun exportKey(password: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val key = getKeyFromKeychain()
                ?: throw EncryptionException("Encryption key not found")

            // Generate salt
            val salt = ByteArray(SALT_LENGTH)
            SecRandomCopyBytes(kSecRandomDefault, SALT_LENGTH.toULong(), salt.refTo(0))

            // Derive key from password using PBKDF2
            val derivedKey = deriveKeyFromPassword(password, salt)

            // Generate IV
            val iv = ByteArray(IV_LENGTH)
            SecRandomCopyBytes(kSecRandomDefault, IV_LENGTH.toULong(), iv.refTo(0))

            // Encrypt the key
            val encryptedKeyBuffer = ByteArray(key.size + TAG_LENGTH)
            val tagBuffer = ByteArray(TAG_LENGTH)

            key.usePinned { keyPin ->
                derivedKey.usePinned { derivedKeyPin ->
                    iv.usePinned { ivPin ->
                        encryptedKeyBuffer.usePinned { encKeyPin ->
                            tagBuffer.usePinned { tagPin ->
                                val status = CCCryptorGCMOneshotEncrypt(
                                    kCCAlgorithmAES,
                                    derivedKeyPin.addressOf(0),
                                    derivedKey.size.toULong(),
                                    ivPin.addressOf(0),
                                    IV_LENGTH.toULong(),
                                    null,
                                    0u,
                                    keyPin.addressOf(0),
                                    key.size.toULong(),
                                    encKeyPin.addressOf(0),
                                    tagPin.addressOf(0),
                                    TAG_LENGTH.toULong()
                                )

                                if (status != kCCSuccess) {
                                    throw EncryptionException("Key encryption failed")
                                }
                            }
                        }
                    }
                }
            }

            val encryptedKey = encryptedKeyBuffer.copyOfRange(0, key.size)

            // Combine: salt:iv:tag:encryptedKey (all base64)
            val backup = "${salt.toNSData().base64EncodedStringWithOptions(0)}:" +
                    "${iv.toNSData().base64EncodedStringWithOptions(0)}:" +
                    "${tagBuffer.toNSData().base64EncodedStringWithOptions(0)}:" +
                    "${encryptedKey.toNSData().base64EncodedStringWithOptions(0)}"

            backup
        }.onFailure { e ->
            Result.failure<String>(EncryptionException("Key export failed", e))
        }
    }

    actual suspend fun importKey(encryptedKeyBackup: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Parse backup
                val parts = encryptedKeyBackup.split(":")
                if (parts.size != 4) {
                    throw EncryptionException("Invalid key backup format")
                }

                val salt = parts[0].fromBase64()
                val iv = parts[1].fromBase64()
                val tag = parts[2].fromBase64()
                val encryptedKey = parts[3].fromBase64()

                // Derive key from password
                val derivedKey = deriveKeyFromPassword(password, salt)

                // Decrypt the key
                val keyBuffer = ByteArray(encryptedKey.size)

                encryptedKey.usePinned { encKeyPin ->
                    derivedKey.usePinned { derivedKeyPin ->
                        iv.usePinned { ivPin ->
                            tag.usePinned { tagPin ->
                                keyBuffer.usePinned { keyPin ->
                                    val status = CCCryptorGCMOneshotDecrypt(
                                        kCCAlgorithmAES,
                                        derivedKeyPin.addressOf(0),
                                        derivedKey.size.toULong(),
                                        ivPin.addressOf(0),
                                        IV_LENGTH.toULong(),
                                        null,
                                        0u,
                                        encKeyPin.addressOf(0),
                                        encryptedKey.size.toULong(),
                                        keyPin.addressOf(0),
                                        tagPin.addressOf(0),
                                        TAG_LENGTH.toULong()
                                    )

                                    if (status != kCCSuccess) {
                                        throw EncryptionException("Key decryption failed")
                                    }
                                }
                            }
                        }
                    }
                }

                // Delete existing key if any
                deleteKeyInternal()

                // Store the imported key
                val query = mutableMapOf<Any?, Any?>()
                query[kSecClass] = kSecClassGenericPassword
                query[kSecAttrAccount] = KEY_ACCOUNT
                query[kSecAttrService] = KEY_SERVICE
                query[kSecValueData] = keyBuffer.toNSData()
                query[kSecAttrAccessible] = kSecAttrAccessibleAfterFirstUnlock

                val status = SecItemAdd(query as CFDictionaryRef, null)
                if (status != errSecSuccess) {
                    throw EncryptionException("Failed to store imported key: $status")
                }
            }.onFailure { e ->
                Result.failure<Unit>(EncryptionException("Key import failed", e))
            }
        }

    actual suspend fun deleteKey(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            deleteKeyInternal()
        }.onFailure { e ->
            Result.failure<Unit>(EncryptionException("Key deletion failed", e))
        }
    }

    private fun getOrCreateKey(): ByteArray {
        return getKeyFromKeychain() ?: run {
            // Generate key if it doesn't exist
            val key = ByteArray(KEY_SIZE)
            SecRandomCopyBytes(kSecRandomDefault, KEY_SIZE.toULong(), key.refTo(0))

            // Store in Keychain
            val query = mutableMapOf<Any?, Any?>()
            query[kSecClass] = kSecClassGenericPassword
            query[kSecAttrAccount] = KEY_ACCOUNT
            query[kSecAttrService] = KEY_SERVICE
            query[kSecValueData] = key.toNSData()
            query[kSecAttrAccessible] = kSecAttrAccessibleAfterFirstUnlock

            SecItemAdd(query as CFDictionaryRef, null)
            key
        }
    }

    private fun getKeyFromKeychain(): ByteArray? {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = KEY_ACCOUNT
        query[kSecAttrService] = KEY_SERVICE
        query[kSecReturnData] = kCFBooleanTrue
        query[kSecMatchLimit] = kSecMatchLimitOne

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)

            if (status == errSecSuccess) {
                val data = result.value as NSData
                return data.toByteArray()
            }
        }

        return null
    }

    private fun deleteKeyInternal() {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = KEY_ACCOUNT
        query[kSecAttrService] = KEY_SERVICE

        SecItemDelete(query as CFDictionaryRef)
    }

    private fun deriveKeyFromPassword(password: String, salt: ByteArray): ByteArray {
        val derivedKey = ByteArray(KEY_SIZE)

        password.encodeToByteArray().usePinned { passwordPin ->
            salt.usePinned { saltPin ->
                derivedKey.usePinned { derivedKeyPin ->
                    CCKeyDerivationPBKDF(
                        kCCPBKDF2,
                        passwordPin.addressOf(0),
                        password.length.toULong(),
                        saltPin.addressOf(0),
                        salt.size.toULong(),
                        kCCPRFHmacAlgSHA256.toUInt(),
                        PBKDF2_ITERATIONS.toUInt(),
                        derivedKeyPin.addressOf(0),
                        KEY_SIZE.toULong()
                    )
                }
            }
        }

        return derivedKey
    }

    private fun ByteArray.toNSData(): NSData {
        return NSData.create(bytes = this.refTo(0), length = this.size.toULong())
    }

    private fun NSData.toByteArray(): ByteArray {
        return ByteArray(this.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
            }
        }
    }

    private fun String.fromBase64(): ByteArray {
        val nsData = NSData.create(base64Encoding = this)
            ?: throw EncryptionException("Invalid base64 data")
        return nsData.toByteArray()
    }

    companion object {
        private const val KEY_SERVICE = "com.po4yka.trailglass.e2e"
        private const val KEY_ACCOUNT = "encryption_key"
        private const val KEY_SIZE = 32 // 256 bits
        private const val IV_LENGTH = 12 // 96 bits for GCM
        private const val TAG_LENGTH = 16 // 128 bits
        private const val SALT_LENGTH = 32
        private const val PBKDF2_ITERATIONS = 100_000
    }
}
