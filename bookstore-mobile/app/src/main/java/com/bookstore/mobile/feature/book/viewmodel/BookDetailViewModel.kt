package com.bookstore.mobile.feature.book.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.book.data.BookRepository
import com.bookstore.mobile.feature.cart.data.CartRepository
import com.bookstore.mobile.shared.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val isLoading: Boolean = true,
    val isAdding: Boolean = false,
    val book: Book? = null,
    val quantity: Int = 1,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class BookDetailViewModel(
    private val bookRepository: BookRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun load(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = bookRepository.getBookDetail(bookId)) {
                is ResultState.Success -> _uiState.update {
                    it.copy(isLoading = false, book = result.data)
                }
                is ResultState.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun increase() = _uiState.update { it.copy(quantity = it.quantity + 1) }

    fun decrease() = _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    fun addToCart(onLoginRequired: () -> Unit) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAdding = true, errorMessage = null, successMessage = null) }
            when (val result = cartRepository.addItem(book.id, _uiState.value.quantity)) {
                is ResultState.Success -> _uiState.update {
                    it.copy(isAdding = false, successMessage = "Da them vao gio hang")
                }
                is ResultState.Error -> {
                    if (result.message.contains("401") || result.message.contains("Unauthorized", ignoreCase = true)) {
                        onLoginRequired()
                    }
                    _uiState.update { it.copy(isAdding = false, errorMessage = result.message) }
                }
                else -> Unit
            }
        }
    }
}
