package com.bookstore.mobile.feature.checkout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
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
    paymentMethod: String,
    paymentStatus: String,
    orderStatus: String,
    transferContent: String,
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
        androidx.compose.material3.Icon(
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
            text = "Ma don: ${orderCode.ifBlank { orderId }}",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        PriceText(totalAmount, modifier = Modifier.padding(top = 12.dp))
        Text("Trang thai don: ${orderStatus.ifBlank { "PENDING" }}", modifier = Modifier.padding(top = 12.dp))
        Text(
            "Thanh toan: ${paymentMethod.ifBlank { "Dang cap nhat" }} - ${paymentStatus.ifBlank { "PENDING" }}",
            modifier = Modifier.padding(top = 4.dp),
        )

        if (paymentMethod == "BANK_TRANSFER_QR" && paymentStatus == "PENDING") {
            Text(
                "Don hang dang cho thanh toan. Hay chuyen khoan dung noi dung ben duoi.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (transferContent.isNotBlank()) {
            Text("Noi dung chuyen khoan", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
            Text(
                transferContent,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Button(onClick = onOrdersClick, modifier = Modifier.padding(top = 20.dp)) {
            Text("Xem chi tiet don")
        }
        Button(onClick = onHomeClick, modifier = Modifier.padding(top = 8.dp)) {
            Text("Ve trang chu")
        }
    }
}
