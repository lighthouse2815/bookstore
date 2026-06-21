package com.bookstore.mobile.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsPreferences by preferencesDataStore(name = "bookstore_settings")

const val DEFAULT_API_BASE_URL = "http://192.168.1.10:8080"

private val OLD_DEFAULT_API_BASE_URLS = setOf(
    "http://192.168.1.15:8080",
)

class AppSettingsDataStore(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("api_base_url")

    val baseUrl: Flow<String> = context.settingsPreferences.data.map {
        val savedBaseUrl = it[baseUrlKey]?.trim()
        when {
            savedBaseUrl.isNullOrBlank() -> DEFAULT_API_BASE_URL
            savedBaseUrl in OLD_DEFAULT_API_BASE_URLS -> DEFAULT_API_BASE_URL
            else -> savedBaseUrl
        }
    }

    suspend fun getBaseUrl(): String = baseUrl.first()

    suspend fun saveBaseUrl(value: String) {
        context.settingsPreferences.edit { preferences ->
            preferences[baseUrlKey] = value.trim().ifBlank { DEFAULT_API_BASE_URL }
        }
    }
}
