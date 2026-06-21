package com.bookstore.mobile.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.auth.data.AuthRepository
import com.bookstore.mobile.feature.profile.data.ProfileRepository
import com.bookstore.mobile.shared.model.Profile
import com.bookstore.mobile.shared.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val user: User? = null,
    val profile: Profile? = null,
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = (profileRepository.getCurrentUser() as? ResultState.Success)?.data
            val profile = (profileRepository.getProfile() as? ResultState.Success)?.data
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    profile = profile,
                    firstName = profile?.firstName.orEmpty(),
                    lastName = profile?.lastName.orEmpty(),
                    phone = user?.phoneNumber.orEmpty(),
                    errorMessage = if (user == null) "Không lấy được tài khoản" else null,
                )
            }
        }
    }

    fun updateFirstName(value: String) = _uiState.update { it.copy(firstName = value, errorMessage = null) }
    fun updateLastName(value: String) = _uiState.update { it.copy(lastName = value, errorMessage = null) }
    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }

    fun save() {
        val current = _uiState.value
        val user = current.user ?: return
        val profile = current.profile ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            when (val result = profileRepository.updateProfile(
                user = user,
                profile = profile,
                firstName = current.firstName,
                lastName = current.lastName,
                phone = current.phone,
            )) {
                is ResultState.Success -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        user = result.data.first,
                        profile = result.data.second,
                        successMessage = "Đã cập nhật hồ sơ",
                    )
                }
                is ResultState.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
