package com.bookstore.mobile.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.auth.data.AuthRepository
import com.bookstore.mobile.shared.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val password: String = "",
    val user: User? = null,
    val errorMessage: String? = null,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun checkSession(
        onAuthenticated: () -> Unit,
        onUnauthenticated: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (!authRepository.hasAccessToken()) {
                _uiState.update { it.copy(isLoading = false) }
                onUnauthenticated()
                return@launch
            }

            when (val userResult = authRepository.getCurrentUser()) {
                is ResultState.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = userResult.data) }
                    onAuthenticated()
                }
                is ResultState.Error -> {
                    when (val refreshResult = authRepository.refreshSession()) {
                        is ResultState.Success -> {
                            _uiState.update { it.copy(isLoading = false, user = refreshResult.data) }
                            onAuthenticated()
                        }
                        is ResultState.Error -> {
                            authRepository.clearSession()
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = refreshResult.message)
                            }
                            onUnauthenticated()
                        }
                        else -> Unit
                    }
                }
                else -> Unit
            }
        }
    }

    fun login(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhap email va mat khau") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.login(current.username, current.password)) {
                is ResultState.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, user = result.data, password = "")
                    }
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
