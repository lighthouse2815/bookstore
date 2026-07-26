package com.bookstore.mobile.feature.order.data.dto

import com.bookstore.mobile.shared.model.OrderTimelineEvent
import kotlinx.serialization.Serializable

@Serializable
data class OrderTimelineEventDto(
    val id: String,
    val orderId: String,
    val eventType: String,
    val title: String,
    val description: String? = null,
    val oldStatus: String? = null,
    val newStatus: String? = null,
    val actorName: String? = null,
    val actorRole: String? = null,
    val createdAt: String,
    val metadata: String? = null,
) {
    fun toModel(): OrderTimelineEvent = OrderTimelineEvent(
        id = id,
        eventType = eventType,
        title = title,
        description = description,
        oldStatus = oldStatus,
        newStatus = newStatus,
        actorName = actorName,
        actorRole = actorRole,
        createdAt = createdAt,
    )
}
