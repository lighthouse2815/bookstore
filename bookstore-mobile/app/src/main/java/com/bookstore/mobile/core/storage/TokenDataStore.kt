package com.bookstore.mobile.core.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first

private val Context.tokenPreferences by preferencesDataStore(name = "bookstore_tokens")

class TokenDataStore(private val context: Context) {
    private val encryptedPreferences by lazy {
        val masterKey = MasterKey.Builder(context, "bookstore_token_master_key")
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "bookstore_secure_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val encryptedAccessTokenKey = "access_token"
    private val encryptedRefreshTokenKey = "refresh_token"

    val accessToken: Flow<String?> = flow { emit(getAccessToken()) }
    val refreshToken: Flow<String?> = flow { emit(getRefreshToken()) }

    suspend fun getAccessToken(): String? {
        migrateLegacyTokensIfNeeded()
        return encryptedPreferences.getString(encryptedAccessTokenKey, null)
    }

    suspend fun getRefreshToken(): String? {
        migrateLegacyTokensIfNeeded()
        return encryptedPreferences.getString(encryptedRefreshTokenKey, null)
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        encryptedPreferences.edit()
            .putString(encryptedAccessTokenKey, accessToken)
            .apply {
                if (refreshToken.isNullOrBlank()) {
                    remove(encryptedRefreshTokenKey)
                } else {
                    putString(encryptedRefreshTokenKey, refreshToken)
                }
            }
            .commit()
        context.tokenPreferences.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    suspend fun clear() {
        encryptedPreferences.edit()
            .remove(encryptedAccessTokenKey)
            .remove(encryptedRefreshTokenKey)
            .commit()
        context.tokenPreferences.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    private suspend fun migrateLegacyTokensIfNeeded() {
        if (!encryptedPreferences.getString(encryptedAccessTokenKey, null).isNullOrBlank()) {
            return
        }

        val legacy = context.tokenPreferences.data.first()
        val legacyAccessToken = legacy[accessTokenKey]
        val legacyRefreshToken = legacy[refreshTokenKey]
        if (legacyAccessToken.isNullOrBlank()) {
            return
        }

        val committed = encryptedPreferences.edit()
            .putString(encryptedAccessTokenKey, legacyAccessToken)
            .apply {
                if (legacyRefreshToken.isNullOrBlank()) remove(encryptedRefreshTokenKey)
                else putString(encryptedRefreshTokenKey, legacyRefreshToken)
            }
            .commit()
        if (committed) {
            context.tokenPreferences.edit { preferences ->
                preferences.remove(accessTokenKey)
                preferences.remove(refreshTokenKey)
            }
        }
    }
}
