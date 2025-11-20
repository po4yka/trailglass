package com.po4yka.trailglass.data.remote.auth

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import me.tatarka.inject.annotations.Inject

/**
 * iOS implementation using Keychain Services.
 */
@Inject
actual class SecureTokenStorage {

    actual suspend fun saveTokens(tokens: AuthTokens) {
        saveToKeychain(KEY_ACCESS_TOKEN, tokens.accessToken)
        saveToKeychain(KEY_REFRESH_TOKEN, tokens.refreshToken)
        saveToKeychain(KEY_EXPIRES_AT, tokens.expiresAt.toString())
    }

    actual suspend fun getTokens(): AuthTokens? {
        val accessToken = getFromKeychain(KEY_ACCESS_TOKEN)
        val refreshToken = getFromKeychain(KEY_REFRESH_TOKEN)
        val expiresAt = getFromKeychain(KEY_EXPIRES_AT)?.toLongOrNull()

        return if (accessToken != null && refreshToken != null && expiresAt != null) {
            AuthTokens(accessToken, refreshToken, expiresAt)
        } else {
            null
        }
    }

    actual suspend fun clearTokens() {
        deleteFromKeychain(KEY_ACCESS_TOKEN)
        deleteFromKeychain(KEY_REFRESH_TOKEN)
        deleteFromKeychain(KEY_EXPIRES_AT)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun saveToKeychain(key: String, value: String) {
        val query = createKeychainQuery(key)

        // Delete existing item
        SecItemDelete(query as CFDictionaryRef)

        // Add new item
        val addQuery = CFDictionaryCreateMutableCopy(null, 0, query as CFDictionaryRef)
        val data = value.toNSData()
        CFDictionarySetValue(
            addQuery,
            kSecValueData,
            CFBridgingRetain(data)
        )

        val status = SecItemAdd(addQuery as CFDictionaryRef, null)
        if (status != errSecSuccess) {
            println("Keychain save failed for key: $key, status: $status")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getFromKeychain(key: String): String? {
        val query = createKeychainQuery(key)

        val searchQuery = CFDictionaryCreateMutableCopy(null, 0, query as CFDictionaryRef)
        CFDictionarySetValue(searchQuery, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(searchQuery, kSecMatchLimit, kSecMatchLimitOne)

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(searchQuery as CFDictionaryRef, result.ptr)

            return if (status == errSecSuccess) {
                val data = result.value as NSData
                NSString.create(data, NSUTF8StringEncoding) as String
            } else {
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteFromKeychain(key: String) {
        val query = createKeychainQuery(key)
        SecItemDelete(query as CFDictionaryRef)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun createKeychainQuery(key: String): CFDictionaryRef {
        return CFDictionaryCreateMutable(
            null,
            4,
            null,
            null
        )!!.apply {
            CFDictionarySetValue(this, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(this, kSecAttrService, SERVICE_NAME.toCFString())
            CFDictionarySetValue(this, kSecAttrAccount, key.toCFString())
            CFDictionarySetValue(this, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
        } as CFDictionaryRef
    }

    private fun String.toNSData(): NSData {
        return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun String.toCFString(): CFStringRef {
        return CFStringCreateWithCString(null, this, kCFStringEncodingUTF8)!!
    }

    companion object {
        private const val SERVICE_NAME = "com.po4yka.trailglass"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
