package com.po4yka.trailglass.data.remote.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import com.po4yka.trailglass.data.remote.auth.TokenStorage
import me.tatarka.inject.annotations.Inject
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryCreateMutableCopy
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private val logger = KotlinLogging.logger {}

/** iOS implementation using Keychain Services. */
@Inject
class IOSSecureTokenStorage : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens) {
        saveToKeychain(KEY_ACCESS_TOKEN, tokens.accessToken)
        saveToKeychain(KEY_REFRESH_TOKEN, tokens.refreshToken)
        saveToKeychain(KEY_EXPIRES_AT, tokens.expiresAt.toString())
    }

    override suspend fun getTokens(): AuthTokens? {
        val accessToken = getFromKeychain(KEY_ACCESS_TOKEN)
        val refreshToken = getFromKeychain(KEY_REFRESH_TOKEN)
        val expiresAt = getFromKeychain(KEY_EXPIRES_AT)?.toLongOrNull()

        return if (accessToken != null && refreshToken != null && expiresAt != null) {
            AuthTokens(accessToken, refreshToken, expiresAt)
        } else {
            null
        }
    }

    override suspend fun clearTokens() {
        deleteFromKeychain(KEY_ACCESS_TOKEN)
        deleteFromKeychain(KEY_REFRESH_TOKEN)
        deleteFromKeychain(KEY_EXPIRES_AT)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun saveToKeychain(
        key: String,
        value: String
    ) {
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
            logger.error { "Keychain save failed for key: $key, status: $status" }
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
    private fun createKeychainQuery(key: String): CFDictionaryRef =
        CFDictionaryCreateMutable(
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

    private fun String.toNSData(): NSData = (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!

    @OptIn(ExperimentalForeignApi::class)
    private fun String.toCFString(): CFStringRef = CFStringCreateWithCString(null, this, kCFStringEncodingUTF8)!!

    companion object {
        private const val SERVICE_NAME = "com.po4yka.trailglass"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
