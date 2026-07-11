package com.bookstore.mobile.feature.order.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
    var isCancelDialogOpen by rememberSaveable { mutableStateOf(false) }
    var cancelReason by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(orderId) { viewModel.load(orderId) }
    LaunchedEffect(state.order?.status) {
        if (state.order?.status == "CANCELLED") {
            isCancelDialogOpen = false
            cancelReason = ""
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Chi tiet don", onBack = onBack) },
    ) { padding ->
        when {
            state.isLoading -> LoadingView()
            state.errorMessage != null -> ErrorView(
                message = state.errorMessage.orEmpty(),
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
                                Text("Ma don: ${order.orderCode}", fontWeight = FontWeight.SemiBold)
                                Text("Order ID: ${order.id}", style = MaterialTheme.typography.bodySmall)
                                Text("Ngay dat: ${formatDateTime(order.createdAt)}")
                                Text("Trang thai: ${order.status}")
                                Text("Thanh toan: ${order.paymentMethod} - ${order.paymentStatus}")
                                Text("Nguoi nhan: ${order.receiverName}")
                                Text("Dien thoai: ${order.receiverPhone}")
                                Text("Dia chi: ${order.receiverAddress}")
                            }
                        }
                    }
                    if (order.status == "PENDING" && order.paymentStatus == "PENDING") {
                        item {
                            Button(
                                onClick = { isCancelDialogOpen = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isCancellingOrder,
                            ) {
                                Text(if (state.isCancellingOrder) "Dang huy don..." else "Huy don hang")
                            }
                        }
                    }
                    state.cancelErrorMessage?.let { message ->
                        item {
                            Text(message, color = MaterialTheme.colorScheme.error)
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
                    item {
                        Text("Hanh trinh don hang", style = MaterialTheme.typography.titleLarge)
                    }
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                when {
                                    state.isTimelineLoading -> Text("Dang tai hanh trinh don hang...")
                                    state.timelineErrorMessage != null -> Text(
                                        "Khong tai duoc hanh trinh: ${state.timelineErrorMessage}",
                                        color = MaterialTheme.colorScheme.error,
                                    )

                                    state.timeline.isEmpty() -> Text("Chua co moc hanh trinh nao.")
                                    else -> state.timeline.forEach { event ->
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(event.title, fontWeight = FontWeight.SemiBold)
                                            event.description?.takeIf { it.isNotBlank() }?.let { description ->
                                                Text(description)
                                            }
                                            Text(
                                                formatDateTime(event.createdAt),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isCancelDialogOpen) {
        AlertDialog(
            onDismissRequest = { if (!state.isCancellingOrder) isCancelDialogOpen = false },
            title = { Text("Xac nhan huy don") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hay nhap ly do huy don. Hang ton va uu dai se duoc hoan lai.")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it.take(500) },
                        label = { Text("Ly do huy don") },
                        enabled = !state.isCancellingOrder,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.cancelOrder(orderId, cancelReason) },
                    enabled = cancelReason.trim().isNotEmpty() && !state.isCancellingOrder,
                ) {
                    Text(if (state.isCancellingOrder) "Dang huy..." else "Xac nhan huy")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isCancelDialogOpen = false },
                    enabled = !state.isCancellingOrder,
                ) { Text("Quay lai") }
            },
        )
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
