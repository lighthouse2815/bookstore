package com.bookstore.mobile.feature.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.cart.data.CartRepository
import com.bookstore.mobile.shared.model.Cart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val isLoading: Boolean = true,
    val cart: Cart? = null,
    val errorMessage: String? = null,
)

class CartViewModel(
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = cartRepository.getCart()) {
                is ResultState.Success -> _uiState.update {
                    it.copy(isLoading = false, cart = result.data)
                }
                is ResultState.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun updateQuantity(bookId: String, quantity: Int) {
        if (quantity <= 0) return
        viewModelScope.launch {
            when (val result = cartRepository.updateItem(bookId, quantity)) {
                is ResultState.Success -> _uiState.update { it.copy(cart = result.data, errorMessage = null) }
                is ResultState.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun remove(bookId: String) {
        viewModelScope.launch {
            when (val result = cartRepository.removeItem(bookId)) {
                is ResultState.Success -> load()
                is ResultState.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
        }
    }
}
