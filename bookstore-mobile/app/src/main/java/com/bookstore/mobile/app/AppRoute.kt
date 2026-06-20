package com.bookstore.mobile.app

import android.net.Uri

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object OtpVerification : AppRoute("otpVerification/{email}") {
        fun create(email: String) = "otpVerification/${Uri.encode(email)}"
    }
    data object Home : AppRoute("main")
    data object BookList : AppRoute("bookList")
    data object BookDetail : AppRoute("bookDetail/{bookId}") {
        fun create(bookId: String) = "bookDetail/$bookId"
    }
    data object Cart : AppRoute("cart")
    data object Checkout : AppRoute("checkout")
    data object OrderSuccess : AppRoute("orderSuccess/{orderId}?orderCode={orderCode}&totalAmount={totalAmount}") {
        fun create(orderId: String, orderCode: String, totalAmount: Double): String {
            return "orderSuccess/${Uri.encode(orderId)}?orderCode=${Uri.encode(orderCode)}&totalAmount=$totalAmount"
        }
    }
    data object Orders : AppRoute("orders")
    data object OrderDetail : AppRoute("orderDetail/{orderId}") {
        fun create(orderId: String) = "orderDetail/$orderId"
    }
    data object Profile : AppRoute("profile")
    data object Settings : AppRoute("settings")
}

val BottomRoutes = listOf(
    AppRoute.Home.route,
    AppRoute.BookList.route,
    AppRoute.Cart.route,
    AppRoute.Orders.route,
    AppRoute.Profile.route,
)
