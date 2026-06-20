package com.bookstore.mobile.feature.book.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bookstore.mobile.core.ui.AppTopBar
import com.bookstore.mobile.core.ui.ErrorView
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.feature.book.ui.component.QuantitySelector
import com.bookstore.mobile.feature.book.viewmodel.BookDetailViewModel
import com.bookstore.mobile.shared.ui.PriceText
import com.bookstore.mobile.shared.ui.PrimaryButton

@Composable
fun BookDetailScreen(
    bookId: String,
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    onLoginRequired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(bookId) { viewModel.load(bookId) }
    LaunchedEffect(state.successMessage, state.errorMessage) {
        val message = state.successMessage ?: state.errorMessage
        if (!message.isNullOrBlank()) snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Chi tiet sach", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> LoadingView()
            state.errorMessage != null && state.book == null -> ErrorView(
                message = state.errorMessage ?: "",
                onRetry = { viewModel.load(bookId) },
            )
            state.book != null -> {
                val book = state.book!!
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        if (book.coverUrl != null) {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = book.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f),
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {}
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(book.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            PriceText(book.price)
                            Text("Tac gia: ${book.author.ifBlank { "Chua ro" }}")
                            Text("Danh muc: ${book.category.ifBlank { "Chua ro" }}")
                            Text("Nha xuat ban: ${book.publisher.ifBlank { "Chua ro" }}")
                            Text("Rating: ${book.rating ?: 0.0} (${book.reviewCount})")
                            Text("Ton kho: ${book.stockQuantity}")
                        }
                    }
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("Mo ta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(book.description ?: "Chua co mo ta")
                                book.detail?.let { detail ->
                                    Text("So trang: ${detail.pageCount ?: "-"}")
                                    Text("Nam xuat ban: ${detail.publicationYear ?: "-"}")
                                    Text("Ngon ngu: ${detail.language ?: "-"}")
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            QuantitySelector(
                                quantity = state.quantity,
                                onDecrease = viewModel::decrease,
                                onIncrease = viewModel::increase,
                            )
                        }
                    }
                    item {
                        PrimaryButton(
                            text = "Them vao gio",
                            isLoading = state.isAdding,
                            onClick = { viewModel.addToCart(onLoginRequired) },
                            enabled = book.stockQuantity > 0,
                        )
                    }
                    item { Box(Modifier.padding(bottom = 24.dp)) }
                }
            }
        }
    }
}
