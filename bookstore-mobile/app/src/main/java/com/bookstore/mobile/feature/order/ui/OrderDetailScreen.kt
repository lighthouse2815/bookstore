package com.bookstore.mobile.feature.order.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.core.ui.AppTopBar
import com.bookstore.mobile.core.ui.ErrorView
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.core.util.formatDateTime
import com.bookstore.mobile.feature.order.viewmodel.OrderDetailViewModel
import com.bookstore.mobile.shared.ui.PriceText

@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrderDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(orderId) { viewModel.load(orderId) }

    Scaffold(
        topBar = { AppTopBar(title = "Chi tiet don", onBack = onBack) },
    ) { padding ->
        when {
            state.isLoading -> LoadingView()
            state.errorMessage != null -> ErrorView(
                message = state.errorMessage ?: "",
                onRetry = { viewModel.load(orderId) },
            )
            state.order != null -> {
                val order = state.order!!
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("Don ${order.id}", fontWeight = FontWeight.SemiBold)
                                Text("Ngay dat: ${formatDateTime(order.createdAt)}")
                                Text("Trang thai: ${order.status}")
                                Text("Thanh toan: ${order.paymentMethod} - ${order.paymentStatus}")
                                Text("Nguoi nhan: ${order.receiverName}")
                                Text("Dien thoai: ${order.receiverPhone}")
                                Text("Dia chi: ${order.receiverAddress}")
                            }
                        }
                    }
                    item {
                        Text("San pham", style = MaterialTheme.typography.titleLarge)
                    }
                    order.items.forEach { item ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.SemiBold)
                                    Text("${item.quantity} x")
                                }
                                PriceText(item.lineTotal)
                            }
                        }
                    }
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                SummaryRow("Tam tinh", order.productTotal)
                                SummaryRow("Phi ship", order.shippingFee)
                                SummaryRow("Giam gia", order.couponDiscount + order.shippingDiscount)
                                SummaryRow("Tong", order.totalAmount)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        PriceText(value)
    }
}
