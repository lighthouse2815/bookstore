package com.bookstore.mobile.feature.checkout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
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
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onSuccess: (CheckoutResult) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { AppTopBar(title = "Checkout", onBack = onBack) },
    ) { padding ->
        when {
            state.isLoading -> LoadingView()
            state.cart == null && state.errorMessage != null -> ErrorView(
                message = state.errorMessage ?: "",
                onRetry = viewModel::load,
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("San pham", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    state.cart?.items.orEmpty().forEach { item ->
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
                        Text("Tong", fontWeight = FontWeight.SemiBold)
                        PriceText(state.cart?.totalAmount ?: 0.0)
                    }
                }
                item {
                    Text("Thong tin giao hang", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                item {
                    TextFieldWithLabel(
                        label = "Full name",
                        value = state.fullName,
                        onValueChange = viewModel::updateFullName,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    TextFieldWithLabel(
                        label = "Phone",
                        value = state.phone,
                        onValueChange = viewModel::updatePhone,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                }
                item {
                    TextFieldWithLabel(
                        label = "Address detail",
                        value = state.addressDetail,
                        onValueChange = viewModel::updateAddress,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                    )
                }
                item {
                    TextFieldWithLabel(
                        label = "Province/city",
                        value = state.provinceCity,
                        onValueChange = viewModel::updateProvinceCity,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Text("Payment method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = true, onClick = {})
                        Text("BANK_TRANSFER_QR")
                    }
                }
                item {
                    TextFieldWithLabel(
                        label = "Coupon code",
                        value = state.couponCode,
                        onValueChange = viewModel::updateCoupon,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    TextFieldWithLabel(
                        label = "Note",
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
                    )
                }
            }
        }
    }
}
