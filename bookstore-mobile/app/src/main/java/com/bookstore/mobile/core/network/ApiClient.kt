package com.bookstore.mobile.core.network

import com.bookstore.mobile.core.storage.AppSettingsDataStore
import com.bookstore.mobile.core.storage.TokenDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

class ApiClient(
    private val tokenDataStore: TokenDataStore,
    private val settingsDataStore: AppSettingsDataStore,
) {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    private var cachedBaseUrl: String? = null
    private var cachedService: ApiService? = null

    fun service(): ApiService {
        val baseUrl = runBlocking { normalizeBaseUrl(settingsDataStore.getBaseUrl()) }
        val existing = cachedService
        if (existing != null && cachedBaseUrl == baseUrl) return existing

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenDataStore))
            .addInterceptor(logging)
            .authenticator(TokenAuthenticator(tokenDataStore, settingsDataStore, json))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(ApiService::class.java).also {
            cachedBaseUrl = baseUrl
            cachedService = it
        }
    }

    fun reset() {
        cachedBaseUrl = null
        cachedService = null
    }

    suspend fun rootBaseUrl(): String = normalizeBaseUrl(settingsDataStore.getBaseUrl()).trimEnd('/')

    fun errorMessage(throwable: Throwable, fallback: String = "Da co loi xay ra"): String {
        if (throwable is ApiException) return throwable.message
        if (throwable is HttpException) {
            val raw = throwable.response()?.errorBody()?.string()
            if (!raw.isNullOrBlank()) {
                val message = runCatching {
                    json.parseToJsonElement(raw).jsonObject["message"]?.jsonPrimitive?.content
                }.getOrNull()
                if (!message.isNullOrBlank()) return message
            }
            return throwable.message()
        }
        return throwable.message?.takeIf { it.isNotBlank() } ?: fallback
    }
}

suspend fun <T> ApiClient.safeApiCall(
    fallback: String = "Da co loi xay ra",
    block: suspend () -> ApiResponse<T>,
): Result<ApiResponse<T>> = runCatching {
    val response = block()
    if (!response.success) throw ApiException(response.message ?: fallback)
    response
}
