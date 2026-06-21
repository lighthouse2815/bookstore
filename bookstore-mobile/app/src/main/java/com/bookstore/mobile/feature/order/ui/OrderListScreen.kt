package com.bookstore.mobile.feature.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.core.ui.ErrorView
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.core.util.formatDateTime
import com.bookstore.mobile.core.util.formatVnd
import com.bookstore.mobile.feature.order.viewmodel.OrderListViewModel
import com.bookstore.mobile.shared.model.Order
import com.bookstore.mobile.shared.ui.NotificationBell
import com.bookstore.mobile.shared.ui.OrdersEmptyIllustration
import com.bookstore.mobile.shared.ui.StoreBackgroundGlow
import com.bookstore.mobile.shared.ui.StoreCard
import com.bookstore.mobile.shared.ui.StoreCardBorder
import com.bookstore.mobile.shared.ui.StoreHeader
import com.bookstore.mobile.shared.ui.StorePrimaryButton
import com.bookstore.mobile.shared.ui.StoreSecondaryButton
import com.bookstore.mobile.shared.ui.StoreSubText
import com.bookstore.mobile.shared.ui.StoreText
import com.bookstore.mobile.shared.ui.StorefrontBackground

@Composable
fun OrderListScreen(
    viewModel: OrderListViewModel,
    onOrderClick: (String) -> Unit,
    onShopNow: () -> Unit,
    onFeaturedBooks: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null -> ErrorView(
            message = state.errorMessage ?: "",
            onRetry = viewModel::load,
        )
        state.orders.isEmpty() -> StorefrontBackground {
            StoreBackgroundGlow()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StoreHeader(
                    title = "Đơn hàng",
                    subtitle = "Theo dõi và quản lý đơn hàng của bạn",
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 22.dp),
                    trailing = { NotificationBell() },
                )

                OrdersEmptyIllustration(
                    modifier = Modifier.padding(top = 38.dp),
                )

                Text(
                    text = "Chưa có đơn hàng",
                    color = StoreText,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                    ),
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = "Có vẻ như bạn chưa đặt đơn hàng nào.\nKhám phá những cuốn sách tuyệt vời\nvà đặt đơn đầu tiên của bạn nhé!",
                    color = StoreSubText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )

                StorePrimaryButton(
                    text = "Mua sách ngay",
                    onClick = onShopNow,
                    icon = Icons.Default.ShoppingCartCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp),
                )
                StoreSecondaryButton(
                    text = "Xem sách nổi bật",
                    onClick = onFeaturedBooks,
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                )
            }
        }
        else -> StorefrontBackground {
            StoreBackgroundGlow()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    StoreHeader(
                        title = "Đơn hàng",
                        subtitle = "Theo dõi và quản lý đơn hàng của bạn",
                        modifier = Modifier.padding(top = 22.dp),
                        trailing = { NotificationBell() },
                    )
                }
                items(state.orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit,
) {
    val accent = when {
        order.status.contains("DELIVER", ignoreCase = true) -> Color(0xFF7CF4E0)
        order.status.contains("PENDING", ignoreCase = true) -> Color(0xFFFFC97E)
        else -> Color(0xFFA67CFF)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = StoreCard,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .border(1.dp, StoreCardBorder, RoundedCornerShape(26.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Đơn #${order.id.take(8).uppercase()}",
                    color = StoreText,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = order.status,
                    color = accent,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = formatDateTime(order.createdAt),
                color = StoreSubText,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = order.paymentMethod,
                        color = StoreSubText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "${order.items.size} sản phẩm",
                        color = StoreSubText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = formatVnd(order.totalAmount),
                    color = Color(0xFF62A4FF),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                    ),
                )
            }
            Text(
                text = order.receiverName,
                color = StoreText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .background(Color(0x142D365E), RoundedCornerShape(14.dp))
                    .border(1.dp, StoreCardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}
