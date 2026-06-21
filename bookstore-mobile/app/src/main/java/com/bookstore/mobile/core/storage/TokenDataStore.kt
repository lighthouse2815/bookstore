package com.bookstore.mobile.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenPreferences by preferencesDataStore(name = "bookstore_tokens")

class TokenDataStore(private val context: Context) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    val accessToken: Flow<String?> = context.tokenPreferences.data.map { it[accessTokenKey] }
    val refreshToken: Flow<String?> = context.tokenPreferences.data.map { it[refreshTokenKey] }

    suspend fun getAccessToken(): String? = accessToken.first()

    suspend fun getRefreshToken(): String? = refreshToken.first()

    suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        context.tokenPreferences.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            if (refreshToken.isNullOrBlank()) {
                preferences.remove(refreshTokenKey)
            } else {
                preferences[refreshTokenKey] = refreshToken
            }
        }
    }

    suspend fun clear() {
        context.tokenPreferences.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }
}
