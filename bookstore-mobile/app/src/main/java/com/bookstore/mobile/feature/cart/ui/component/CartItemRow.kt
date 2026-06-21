package com.bookstore.mobile.feature.cart.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bookstore.mobile.feature.book.ui.component.QuantitySelector
import com.bookstore.mobile.shared.model.CartItem
import com.bookstore.mobile.shared.ui.PriceText

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                PriceText(item.price)
                QuantitySelector(
                    quantity = item.quantity,
                    onDecrease = { onQuantityChange(item.quantity - 1) },
                    onIncrease = { onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Xoa")
                }
                Text("Tong", style = MaterialTheme.typography.bodySmall)
                PriceText(item.lineTotal)
            }
        }
    }
}
