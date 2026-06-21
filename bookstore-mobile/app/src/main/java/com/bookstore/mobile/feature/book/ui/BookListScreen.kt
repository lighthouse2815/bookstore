package com.bookstore.mobile.feature.book.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.core.ui.ErrorView
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.feature.book.viewmodel.BookListViewModel
import com.bookstore.mobile.shared.ui.NotificationBell
import com.bookstore.mobile.shared.ui.StoreBackgroundGlow
import com.bookstore.mobile.shared.ui.StoreBookCard
import com.bookstore.mobile.shared.ui.StoreCategoryChip
import com.bookstore.mobile.shared.ui.StoreHeader
import com.bookstore.mobile.shared.ui.StoreSearchBar
import com.bookstore.mobile.shared.ui.StoreSubText
import com.bookstore.mobile.shared.ui.StoreText
import com.bookstore.mobile.shared.ui.StorefrontBackground
import androidx.compose.material3.Text

@Composable
fun BookListScreen(
    viewModel: BookListViewModel,
    onBookClick: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    val selectedCategoryName = state.categories
        .firstOrNull { it.id == state.selectedCategoryId }
        ?.name

    val filteredBooks = state.books.filter { book ->
        val matchesCategory = selectedCategoryName == null || book.category == selectedCategoryName
        matchesCategory
    }

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null && filteredBooks.isEmpty() -> ErrorView(
            message = state.errorMessage ?: "",
            onRetry = viewModel::load,
        )
        else -> StorefrontBackground {
            StoreBackgroundGlow()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    StoreHeader(
                        title = "Sách",
                        subtitle = "",
                        modifier = Modifier.padding(top = 20.dp),
                        trailing = { NotificationBell() },
                    )
                }
                item {
                    StoreSearchBar(
                        placeholder = "Tìm sách, tác giả...",
                        value = state.keyword,
                        onValueChange = viewModel::updateKeyword,
                    )
                }
                item {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StoreCategoryChip(
                            text = "Tất cả",
                            selected = state.selectedCategoryId == null,
                            showIcon = false,
                            onClick = { viewModel.selectCategory(null) },
                        )
                        state.categories.forEach { category ->
                            StoreCategoryChip(
                                text = category.name,
                                selected = state.selectedCategoryId == category.id,
                                onClick = { viewModel.selectCategory(category.id) },
                            )
                        }
                    }
                }
                if (filteredBooks.isEmpty()) {
                    item {
                        Text(
                            text = "Không có sách phù hợp",
                            color = StoreText,
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                        )
                        Text(
                            text = "Thử từ khóa khác hoặc đổi danh mục.",
                            color = StoreSubText,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    items(filteredBooks, key = { it.id }) { book ->
                        StoreBookCard(
                            book = book,
                            onClick = { onBookClick(book.id) },
                        )
                    }
                }
            }
        }
    }
}
