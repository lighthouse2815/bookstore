package com.bookstore.mobile.feature.checkout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookstore.mobile.shared.ui.PriceText

@Composable
fun OrderSuccessScreen(
    orderId: String,
    orderCode: String,
    totalAmount: Double,
    onOrdersClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Dat hang thanh cong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Order ID: $orderId",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (orderCode.isNotBlank()) {
            Text("Order code: $orderCode")
        }
        PriceText(totalAmount, modifier = Modifier.padding(vertical = 12.dp))
        Button(onClick = onOrdersClick) {
            Text("Xem don hang")
        }
        Button(onClick = onHomeClick, modifier = Modifier.padding(top = 8.dp)) {
            Text("Ve Home")
        }
    }
}
