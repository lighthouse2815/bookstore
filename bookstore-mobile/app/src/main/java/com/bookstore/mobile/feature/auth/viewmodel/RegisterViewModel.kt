package com.bookstore.mobile.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun updateOtp(value: String) = _uiState.update { it.copy(otpCode = value.take(6), errorMessage = null) }

    fun register(onOtpRequired: (String) -> Unit) {
        val current = _uiState.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhap email va mat khau") }
            return
        }
        if (current.password != current.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Xac nhan mat khau khong khop") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.register(current.email, current.password)) {
                is ResultState.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, infoMessage = "Da gui OTP den email")
                    }
                    onOtpRequired(result.data)
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun requestOtp(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.requestRegistrationOtp(email)) {
                is ResultState.Success -> _uiState.update {
                    it.copy(isLoading = false, infoMessage = "Da gui lai OTP")
                }
                is ResultState.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun verifyOtp(email: String, onSuccess: () -> Unit) {
        val otpCode = _uiState.value.otpCode
        if (otpCode.length != 6) {
            _uiState.update { it.copy(errorMessage = "OTP gom 6 chu so") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.verifyRegistrationOtp(email, otpCode)) {
                is ResultState.Success -> {
                    _uiState.update { it.copy(isLoading = false, infoMessage = "Xac thuc thanh cong") }
                    onSuccess()
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> Unit
            }
        }
    }
}
