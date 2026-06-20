package com.bookstore.mobile.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bookstore.mobile.app.AppRoute

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val Items = listOf(
    BottomNavItem(AppRoute.Home.route, "Home", Icons.Default.Home),
    BottomNavItem(AppRoute.BookList.route, "Books", Icons.AutoMirrored.Filled.MenuBook),
    BottomNavItem(AppRoute.Cart.route, "Cart", Icons.Default.ShoppingCartCheckout),
    BottomNavItem(AppRoute.Orders.route, "Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    BottomNavItem(AppRoute.Profile.route, "Profile", Icons.Default.PersonOutline),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StoreBackground.copy(alpha = 0.94f))
                .border(
                    width = 1.dp,
                    color = StoreCardBorder,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Items.forEach { item ->
                    val selected = currentRoute == item.route
                    val selectedTint = if (item.route == AppRoute.Orders.route) {
                        Color(0xFF7CF4E0)
                    } else {
                        Color(0xFFF6F3FF)
                    }

                    Surface(
                        onClick = { onNavigate(item.route) },
                        color = Color.Transparent,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .height(86.dp)
                                .background(
                                    brush = if (selected) {
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF6E49D5), Color(0xFF2B234E)),
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent),
                                        )
                                    },
                                    shape = RoundedCornerShape(22.dp),
                                )
                                .border(
                                    width = if (selected) 1.dp else 0.dp,
                                    color = if (selected) Color(0xFF8B73FF) else Color.Transparent,
                                    shape = RoundedCornerShape(22.dp),
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) selectedTint else Color(0xFFD8D4E6),
                                modifier = Modifier.size(30.dp),
                            )
                            Text(
                                text = item.label,
                                color = if (selected) selectedTint else Color(0xFFD8D4E6),
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}
