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
import androidx.compose.material3.Checkbox
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
    onCheckout: (List<String>) -> Unit,
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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Da chon ${state.selectedItems.size}/${state.cart?.items.orEmpty().size} san pham",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(onClick = viewModel::toggleSelectAll) {
                            Text(
                                if (state.selectedItemIds.containsAll(state.cart?.items.orEmpty().map { it.id })) {
                                    "Bo chon tat ca"
                                } else {
                                    "Chon tat ca"
                                },
                            )
                        }
                    }
                }
                items(state.cart?.items.orEmpty(), key = { it.id }) { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.id in state.selectedItemIds,
                            onCheckedChange = { viewModel.toggleSelection(item.id) },
                        )
                        CartItemRow(
                            item = item,
                            onQuantityChange = { quantity -> viewModel.updateQuantity(item.id, quantity) },
                            onRemove = { viewModel.remove(item.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
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
                    PriceText(state.selectedTotalAmount)
                }
                Button(
                    onClick = { viewModel.checkoutSelected()?.let(onCheckout) },
                ) {
                    Text("Thanh toan (${state.selectedItems.size})")
                }
            }
        }
    }
}
