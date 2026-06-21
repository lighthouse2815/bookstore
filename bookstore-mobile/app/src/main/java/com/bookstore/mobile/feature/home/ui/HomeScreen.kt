package com.bookstore.mobile.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.bookstore.mobile.feature.home.viewmodel.HomeViewModel
import com.bookstore.mobile.shared.ui.NotificationBell
import com.bookstore.mobile.shared.ui.StoreBackgroundGlow
import com.bookstore.mobile.shared.ui.StoreBookCard
import com.bookstore.mobile.shared.ui.StoreCategoryChip
import com.bookstore.mobile.shared.ui.StoreHeader
import com.bookstore.mobile.shared.ui.StoreSearchBar
import com.bookstore.mobile.shared.ui.StoreSectionTitle
import com.bookstore.mobile.shared.ui.StorefrontBackground

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBookClick: (String) -> Unit,
    onBooksClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null && state.books.isEmpty() -> ErrorView(
            message = state.errorMessage ?: "",
            onRetry = viewModel::load,
        )
        else -> {
            val greetingName = state.user?.username?.takeIf { it.isNotBlank() } ?: "bạn"

            StorefrontBackground {
                StoreBackgroundGlow()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item {
                        StoreHeader(
                            title = "Xin chào, $greetingName 👋",
                            subtitle = "Khám phá thế giới sách đầy cảm hứng",
                            modifier = Modifier.padding(top = 22.dp),
                            trailing = { NotificationBell() },
                        )
                    }
                    item {
                        Box(modifier = Modifier.clickable(onClick = onBooksClick)) {
                            StoreSearchBar(
                                placeholder = "Tìm kiếm sách, tác giả, thể loại...",
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    if (state.categories.isNotEmpty()) {
                        item {
                            StoreSectionTitle(title = "Danh mục")
                        }
                        item {
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                state.categories.take(4).forEach { category ->
                                    StoreCategoryChip(
                                        text = category.name,
                                        selected = false,
                                        onClick = onBooksClick,
                                    )
                                }
                            }
                        }
                    }
                    item {
                        StoreSectionTitle(
                            title = "Sách nổi bật",
                            action = "Xem tất cả  ›",
                            onActionClick = onBooksClick,
                        )
                    }
                    items(state.books.take(4), key = { it.id }) { book ->
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
