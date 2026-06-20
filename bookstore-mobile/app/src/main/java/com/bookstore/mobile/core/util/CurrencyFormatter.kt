package com.bookstore.mobile.core.util

import java.text.NumberFormat
import java.util.Locale

fun formatVnd(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(value)
}
