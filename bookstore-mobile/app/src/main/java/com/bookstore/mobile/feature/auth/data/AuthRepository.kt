package com.bookstore.mobile.feature.auth.data

import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.storage.TokenDataStore
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.auth.data.dto.LoginRequest
import com.bookstore.mobile.feature.auth.data.dto.LogoutRequest
import com.bookstore.mobile.feature.auth.data.dto.RefreshTokenRequest
import com.bookstore.mobile.feature.auth.data.dto.RegisterRequest
import com.bookstore.mobile.feature.auth.data.dto.RequestRegistrationOtpRequest
import com.bookstore.mobile.feature.auth.data.dto.VerifyOtpRequest
import com.bookstore.mobile.shared.model.User

class AuthRepository(
    private val apiClient: ApiClient,
    private val tokenDataStore: TokenDataStore,
) {
    suspend fun login(username: String, password: String): ResultState<User> = call("Dang nhap that bai") {
        val session = apiClient.service().login(LoginRequest(username.trim(), password)).data
            ?: error("Phan hoi dang nhap khong hop le")
        tokenDataStore.saveTokens(session.accessToken, session.refreshToken)
        getCurrentUserOrSession(session.userId, username, session.status, session.roles)
    }

    suspend fun register(email: String, password: String): ResultState<String> = call("Dang ky that bai") {
        apiClient.service().register(RegisterRequest(email.trim(), password))
        email.trim()
    }

    suspend fun requestRegistrationOtp(email: String): ResultState<Unit> = call("Gui OTP that bai") {
        apiClient.service().requestRegistrationOtp(RequestRegistrationOtpRequest(email.trim()))
        Unit
    }

    suspend fun verifyRegistrationOtp(email: String, otpCode: String): ResultState<Unit> = call("Xac thuc OTP that bai") {
        apiClient.service().verifyRegistrationOtp(VerifyOtpRequest(email.trim(), otpCode.trim()))
        Unit
    }

    suspend fun getCurrentUser(): ResultState<User> = call("Khong lay duoc thong tin tai khoan") {
        apiClient.service().getCurrentUser().data?.toModel()
            ?: error("Phan hoi tai khoan khong hop le")
    }

    suspend fun refreshSession(): ResultState<User> = call("Phien dang nhap da het han") {
        val refreshToken = tokenDataStore.getRefreshToken() ?: error("Thieu refresh token")
        val session = apiClient.service().refresh(RefreshTokenRequest(refreshToken)).data
            ?: error("Phan hoi refresh token khong hop le")
        tokenDataStore.saveTokens(session.accessToken, session.refreshToken)
        getCurrentUserOrSession(session.userId, session.userId, session.status, session.roles)
    }

    suspend fun logout(): ResultState<Unit> {
        val refreshToken = tokenDataStore.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            runCatching { apiClient.service().logout(LogoutRequest(refreshToken)) }
        }
        tokenDataStore.clear()
        return ResultState.Success(Unit)
    }

    suspend fun hasAccessToken(): Boolean = !tokenDataStore.getAccessToken().isNullOrBlank()

    suspend fun clearSession() {
        tokenDataStore.clear()
    }

    private suspend fun getCurrentUserOrSession(
        userId: String,
        username: String,
        status: String,
        roles: List<String>,
    ): User {
        return runCatching {
            apiClient.service().getCurrentUser().data?.toModel()
        }.getOrNull() ?: User(
            id = userId,
            username = username,
            email = username,
            phoneNumber = "",
            status = status,
            roles = roles,
        )
    }

    private suspend fun <T> call(
        fallback: String,
        block: suspend () -> T,
    ): ResultState<T> = runCatching { block() }
        .fold(
            onSuccess = { ResultState.Success(it) },
            onFailure = { ResultState.Error(apiClient.errorMessage(it, fallback)) },
        )
}
