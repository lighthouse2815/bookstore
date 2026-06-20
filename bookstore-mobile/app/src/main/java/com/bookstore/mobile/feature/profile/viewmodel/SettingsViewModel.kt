package com.bookstore.mobile.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.storage.AppSettingsDataStore
import com.bookstore.mobile.core.storage.DEFAULT_API_BASE_URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val baseUrl: String = DEFAULT_API_BASE_URL,
    val successMessage: String? = null,
)

class SettingsViewModel(
    private val settingsDataStore: AppSettingsDataStore,
    private val apiClient: ApiClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = false, baseUrl = settingsDataStore.getBaseUrl())
            }
        }
    }

    fun updateBaseUrl(value: String) {
        _uiState.update { it.copy(baseUrl = value, successMessage = null) }
    }

    fun save() {
        viewModelScope.launch {
            settingsDataStore.saveBaseUrl(_uiState.value.baseUrl)
            apiClient.reset()
            _uiState.update { it.copy(successMessage = "Da luu API base URL") }
        }
    }

    fun resetDefault() {
        _uiState.update { it.copy(baseUrl = DEFAULT_API_BASE_URL, successMessage = null) }
    }
}
