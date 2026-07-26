package com.bookstore.mobile.feature.checkout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.core.ui.AppTopBar
import com.bookstore.mobile.core.ui.ErrorView
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.feature.checkout.viewmodel.CheckoutViewModel
import com.bookstore.mobile.shared.model.CheckoutResult
import com.bookstore.mobile.shared.ui.PriceText
import com.bookstore.mobile.shared.ui.PrimaryButton
import com.bookstore.mobile.shared.ui.TextFieldWithLabel

@Composable
fun CheckoutScreen(
    selectedCartItemIds: List<String>,
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onSuccess: (CheckoutResult) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedItemIdsKey = selectedCartItemIds.joinToString(",")
    LaunchedEffect(selectedItemIdsKey) { viewModel.load(selectedCartItemIds) }

    Scaffold(
        topBar = { AppTopBar(title = "Thanh toan", onBack = onBack) },
    ) { padding ->
        when {
            state.isLoading -> LoadingView()
            state.cart == null && state.errorMessage != null -> ErrorView(
                message = state.errorMessage.orEmpty(),
                onRetry = { viewModel.load(selectedCartItemIds) },
            )

            state.selectedItems.isEmpty() -> ErrorView(
                message = state.errorMessage ?: "Chon it nhat mot san pham de thanh toan",
                onRetry = onBack,
            )

            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("San pham da chon", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    state.selectedItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${item.title} x${item.quantity}", modifier = Modifier.weight(1f))
                            PriceText(item.lineTotal)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Tam tinh", fontWeight = FontWeight.SemiBold)
                        PriceText(state.selectedTotalAmount)
                    }
                }

                item {
                    Text("Dia chi nhan hang", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    state.addressMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                    }
                }

                if (!state.isCreatingNewAddress) {
                    items(state.addresses.size) { index ->
                        val address = state.addresses[index]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = address.id == state.selectedAddressId,
                                onClick = { viewModel.selectAddress(address.id) },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(address.receiverName, fontWeight = FontWeight.SemiBold)
                                Text(address.receiverPhone, style = MaterialTheme.typography.bodySmall)
                                Text(address.receiverAddress, style = MaterialTheme.typography.bodySmall)
                                if (address.defaultAddress) {
                                    Text("Dia chi mac dinh", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    item {
                        Button(onClick = viewModel::startCreatingAddress) {
                            Text("Tao dia chi moi")
                        }
                    }
                } else {
                    item {
                        if (state.addresses.isEmpty()) {
                            Text("Chua co dia chi luu. Hay tao dia chi moi de tiep tuc.")
                        } else {
                            Button(onClick = { viewModel.selectAddress(state.addresses.first().id) }) {
                                Text("Dung dia chi da luu")
                            }
                        }
                    }
                    item {
                        TextFieldWithLabel(
                            label = "Ho va ten",
                            value = state.fullName,
                            onValueChange = viewModel::updateFullName,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item {
                        TextFieldWithLabel(
                            label = "So dien thoai",
                            value = state.phone,
                            onValueChange = viewModel::updatePhone,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        )
                    }
                    item {
                        TextFieldWithLabel(
                            label = "Dia chi chi tiet",
                            value = state.addressDetail,
                            onValueChange = viewModel::updateAddress,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                        )
                    }
                    item {
                        TextFieldWithLabel(
                            label = "Tinh/thanh pho (tuy chon)",
                            value = state.provinceCity,
                            onValueChange = viewModel::updateProvinceCity,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    Text("Phuong thuc giao hang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    CheckoutOption(
                        label = "Giao tan noi",
                        selected = state.shippingMethod == "DELIVERY",
                        onClick = { viewModel.selectShippingMethod("DELIVERY") },
                    )
                    CheckoutOption(
                        label = "Nhan tai cua hang",
                        selected = state.shippingMethod == "PICKUP",
                        onClick = { viewModel.selectShippingMethod("PICKUP") },
                    )
                }

                item {
                    Text("Phuong thuc thanh toan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    CheckoutOption(
                        label = "Chuyen khoan QR",
                        selected = state.paymentMethod == "BANK_TRANSFER_QR",
                        onClick = { viewModel.selectPaymentMethod("BANK_TRANSFER_QR") },
                    )
                    CheckoutOption(
                        label = "Thanh toan khi nhan hang (COD)",
                        selected = state.paymentMethod == "COD",
                        onClick = { viewModel.selectPaymentMethod("COD") },
                    )
                    if (state.paymentMethod == "BANK_TRANSFER_QR") {
                        Text(
                            "Don hang se o trang thai cho thanh toan sau khi tao.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                item {
                    Text("Ma giam gia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextFieldWithLabel(
                        label = "Ma giam gia san pham",
                        value = state.bookCouponCode,
                        onValueChange = viewModel::updateBookCoupon,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    TextFieldWithLabel(
                        label = "Ma giam phi giao hang",
                        value = state.shippingCouponCode,
                        onValueChange = viewModel::updateShippingCoupon,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    state.bestCouponSuggestion?.let { suggestion ->
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(suggestion.label ?: "Ma giam gia phu hop", fontWeight = FontWeight.SemiBold)
                                Text("${suggestion.couponType}: ${suggestion.couponCode}")
                                Text("Uoc tinh sau giam gia")
                                PriceText(suggestion.finalAmountEstimate)
                                Button(onClick = viewModel::applySuggestedCoupon) {
                                    Text("Ap dung goi y")
                                }
                            }
                        }
                    }
                    if (state.isCouponLoading) {
                        Text("Dang tim ma giam gia phu hop...", modifier = Modifier.padding(top = 8.dp))
                    } else {
                        state.couponMessage?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    Button(onClick = viewModel::refreshBestCoupon, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Tai lai goi y")
                    }
                }

                item {
                    TextFieldWithLabel(
                        label = "Ghi chu",
                        value = state.note,
                        onValueChange = viewModel::updateNote,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                    )
                }

                item {
                    state.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    PrimaryButton(
                        text = "Dat hang",
                        isLoading = state.isSubmitting,
                        onClick = { viewModel.submit(onSuccess) },
                        enabled = state.selectedItems.isNotEmpty(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}
