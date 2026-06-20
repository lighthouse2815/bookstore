package com.bookstore.mobile.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.auth.data.AuthRepository
import com.bookstore.mobile.feature.book.data.BookRepository
import com.bookstore.mobile.shared.model.Book
import com.bookstore.mobile.shared.model.Category
import com.bookstore.mobile.shared.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val books: List<Book> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = (authRepository.getCurrentUser() as? ResultState.Success)?.data
            val booksResult = bookRepository.getBooks()
            val categoriesResult = bookRepository.getCategories()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    books = (booksResult as? ResultState.Success)?.data.orEmpty().take(8),
                    categories = (categoriesResult as? ResultState.Success)?.data.orEmpty().take(8),
                    errorMessage = (booksResult as? ResultState.Error)?.message,
                )
            }
        }
    }
}
