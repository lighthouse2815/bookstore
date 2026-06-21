package com.bookstore.mobile.shared.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bookstore.mobile.core.util.formatVnd

@Composable
fun PriceText(
    value: Double,
    modifier: Modifier = Modifier,
) {
    Text(
        text = formatVnd(value),
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium,
    )
}
