package com.bookstore.mobile.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val OrderDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    .withZone(ZoneId.systemDefault())

fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching { OrderDateFormatter.format(Instant.parse(value)) }
        .getOrElse { value }
}
