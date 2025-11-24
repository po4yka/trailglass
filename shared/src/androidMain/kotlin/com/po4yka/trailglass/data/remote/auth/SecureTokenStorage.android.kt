@file:Suppress("DEPRECATION")

package com.po4yka.trailglass.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import com.po4yka.trailglass.data.remote.auth.TokenStorage

/** Android implementation using EncryptedSharedPreferences. */
@Suppress("DEPRECATION")
@Inject
class AndroidSecureTokenStorage(
    private val context: Context
) : TokenStorage {
    private val masterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "trailglass_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveTokens(tokens: AuthTokens): Unit =
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                putString(KEY_ACCESS_TOKEN, tokens.accessToken)
                putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
                putLong(KEY_EXPIRES_AT, tokens.expiresAt)
                apply()
            }
            Unit
        }

    override suspend fun getTokens(): AuthTokens? =
        withContext(Dispatchers.IO) {
            val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            val expiresAt = sharedPreferences.getLong(KEY_EXPIRES_AT, 0)

            if (accessToken != null && refreshToken != null && expiresAt > 0) {
                AuthTokens(accessToken, refreshToken, expiresAt)
            } else {
                null
            }
        }

    override suspend fun clearTokens(): Unit =
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                remove(KEY_ACCESS_TOKEN)
                remove(KEY_REFRESH_TOKEN)
                remove(KEY_EXPIRES_AT)
                apply()
            }
            Unit
        }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
