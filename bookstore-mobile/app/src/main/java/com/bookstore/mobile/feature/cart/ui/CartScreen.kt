package com.bookstore.mobile.feature.cart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.core.ui.EmptyView
import com.bookstore.mobile.core.ui.ErrorView
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.feature.cart.ui.component.CartItemRow
import com.bookstore.mobile.feature.cart.viewmodel.CartViewModel
import com.bookstore.mobile.shared.ui.PriceText

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onCheckout: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null && state.cart == null -> ErrorView(
            message = state.errorMessage ?: "",
            onRetry = viewModel::load,
        )
        state.cart?.items.isNullOrEmpty() -> EmptyView("Gio hang dang trong")
        else -> Column(modifier = Modifier.fillMaxSize()) {
            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.cart?.items.orEmpty(), key = { it.id }) { item ->
                    CartItemRow(
                        item = item,
                        onQuantityChange = { quantity -> viewModel.updateQuantity(item.id, quantity) },
                        onRemove = { viewModel.remove(item.id) },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Tong tien", fontWeight = FontWeight.SemiBold)
                    PriceText(state.cart?.totalAmount ?: 0.0)
                }
                Button(onClick = onCheckout) {
                    Text("Checkout")
                }
            }
        }
    }
}
