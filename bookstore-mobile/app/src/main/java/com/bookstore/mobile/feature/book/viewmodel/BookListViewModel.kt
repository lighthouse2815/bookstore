package com.bookstore.mobile.feature.book.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.book.data.BookRepository
import com.bookstore.mobile.shared.model.Book
import com.bookstore.mobile.shared.model.Category
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookListUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val categories: List<Category> = emptyList(),
    val keyword: String = "",
    val selectedCategoryId: String? = null,
    val errorMessage: String? = null,
)

class BookListViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val booksResult = bookRepository.getBooks(_uiState.value.keyword)
            val categoriesResult = bookRepository.getCategories()
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    books = (booksResult as? ResultState.Success)?.data.orEmpty(),
                    categories = (categoriesResult as? ResultState.Success)?.data.orEmpty(),
                    errorMessage = (booksResult as? ResultState.Error)?.message,
                )
            }
        }
    }

    fun updateKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load()
        }
    }

    fun selectCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }
}
