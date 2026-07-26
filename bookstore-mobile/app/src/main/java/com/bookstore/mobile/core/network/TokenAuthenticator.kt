package com.bookstore.mobile.core.network

import com.bookstore.mobile.core.storage.DEFAULT_API_BASE_URL
import com.bookstore.mobile.core.storage.AppSettingsDataStore
import com.bookstore.mobile.core.storage.TokenDataStore
import com.bookstore.mobile.feature.auth.data.dto.RefreshTokenRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenDataStore: TokenDataStore,
    private val settingsDataStore: AppSettingsDataStore,
    private val json: Json,
) : Authenticator {
    private val refreshClient = OkHttpClient()
    private val mediaType = "application/json".toMediaType()
    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.contains("/api/auth/") || path.contains("/api/otp/")) return null
        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking { tokenDataStore.getRefreshToken() } ?: return null
        val refreshedAccessToken = runBlocking { refreshSession(refreshToken) } ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $refreshedAccessToken")
            .build()
    }

    private suspend fun refreshSession(expectedRefreshToken: String): String? = refreshMutex.withLock {
        val currentRefreshToken = tokenDataStore.getRefreshToken() ?: return@withLock null
        if (currentRefreshToken != expectedRefreshToken) {
            return@withLock tokenDataStore.getAccessToken()
        }

        val baseUrl = normalizeBaseUrl(settingsDataStore.getBaseUrl())
        val body = json.encodeToString(RefreshTokenRequest(currentRefreshToken)).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${baseUrl}api/auth/refresh")
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        val refreshedAccessToken = runCatching {
            refreshClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    null
                } else {
                    val rawBody = response.body.string()
                    val payload = json.decodeFromString(ApiResponse.serializer(com.bookstore.mobile.feature.auth.data.dto.LoginResponse.serializer()), rawBody)
                    val session = payload.data
                    if (session == null) {
                        null
                    } else {
                        tokenDataStore.saveTokens(session.accessToken, session.refreshToken)
                        session.accessToken
                    }
                }
            }
        }.getOrNull()
        if (refreshedAccessToken == null) {
            tokenDataStore.clear()
        }
        refreshedAccessToken
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

fun normalizeBaseUrl(value: String): String {
    val trimmed = value.trim().ifBlank { DEFAULT_API_BASE_URL }
    return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
}
