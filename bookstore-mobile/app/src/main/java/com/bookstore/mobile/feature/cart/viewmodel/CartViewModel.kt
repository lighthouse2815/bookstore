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
    val selectedItemIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
) {
    val selectedItems get() = cart?.items.orEmpty().filter { it.id in selectedItemIds }
    val selectedTotalAmount get() = selectedItems.sumOf { it.lineTotal }
}

class CartViewModel(
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = cartRepository.getCart()) {
                is ResultState.Success -> _uiState.update { current ->
                    val currentItemIds = result.data.items.map { it.id }.toSet()
                    val selectedItemIds = if (current.cart == null) {
                        currentItemIds
                    } else {
                        current.selectedItemIds.intersect(currentItemIds)
                    }
                    current.copy(
                        isLoading = false,
                        cart = result.data,
                        selectedItemIds = selectedItemIds,
                    )
                }
                is ResultState.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun updateQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) return
        viewModelScope.launch {
            when (val result = cartRepository.updateItem(itemId, quantity)) {
                is ResultState.Success -> _uiState.update { it.copy(cart = result.data, errorMessage = null) }
                is ResultState.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun remove(itemId: String) {
        viewModelScope.launch {
            when (val result = cartRepository.removeItem(itemId)) {
                is ResultState.Success -> load()
                is ResultState.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun toggleSelection(itemId: String) {
        _uiState.update { current ->
            val nextSelectedItemIds = current.selectedItemIds.toMutableSet().apply {
                if (!add(itemId)) {
                    remove(itemId)
                }
            }
            current.copy(selectedItemIds = nextSelectedItemIds, errorMessage = null)
        }
    }

    fun toggleSelectAll() {
        _uiState.update { current ->
            val itemIds = current.cart?.items.orEmpty().map { it.id }.toSet()
            current.copy(
                selectedItemIds = if (current.selectedItemIds.containsAll(itemIds)) emptySet() else itemIds,
                errorMessage = null,
            )
        }
    }

    fun checkoutSelected(): List<String>? {
        val selectedItemIds = _uiState.value.selectedItems.map { it.id }
        if (selectedItemIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Chon it nhat mot san pham de thanh toan") }
            return null
        }

        return selectedItemIds
    }
}
