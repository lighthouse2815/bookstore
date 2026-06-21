package com.bookstore.mobile.feature.profile.data

import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.profile.data.dto.UpdateProfileRequest
import com.bookstore.mobile.feature.profile.data.dto.UpdateUserRequest
import com.bookstore.mobile.shared.model.Profile
import com.bookstore.mobile.shared.model.User

class ProfileRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getCurrentUser(): ResultState<User> = call("Khong lay duoc tai khoan") {
        apiClient.service().getCurrentUser().data?.toModel() ?: error("Tai khoan khong hop le")
    }

    suspend fun getProfile(): ResultState<Profile> = call("Khong lay duoc ho so") {
        apiClient.service().getCurrentProfile().data?.toModel() ?: error("Ho so khong hop le")
    }

    suspend fun updateProfile(
        user: User,
        profile: Profile,
        firstName: String,
        lastName: String,
        phone: String,
    ): ResultState<Pair<User, Profile>> = call("Cap nhat ho so that bai") {
        val normalizedPhone = phone.trim()
        val updatedUser = if (normalizedPhone.isBlank()) {
            user
        } else {
            apiClient.service().updateCurrentUser(
                UpdateUserRequest(
                    username = user.username,
                    phoneNumber = normalizedPhone,
                    email = user.email,
                ),
            ).data?.toModel() ?: error("Tai khoan khong hop le")
        }

        val updatedProfile = apiClient.service().updateCurrentProfile(
            UpdateProfileRequest(
                lastName = lastName.trim().ifBlank { profile.lastName.ifBlank { "User" } },
                firstName = firstName.trim().ifBlank { profile.firstName.ifBlank { "Bookstore" } },
                avatarUrl = profile.avatarUrl,
                gender = profile.gender.ifBlank { "OTHER" },
                dateOfBirth = profile.dateOfBirth.ifBlank { "1990-01-01" },
            ),
        ).data?.toModel() ?: error("Ho so khong hop le")

        updatedUser to updatedProfile
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
