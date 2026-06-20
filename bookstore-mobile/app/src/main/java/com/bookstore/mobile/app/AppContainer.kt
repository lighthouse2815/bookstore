package com.bookstore.mobile.app

import android.content.Context
import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.storage.AppSettingsDataStore
import com.bookstore.mobile.core.storage.TokenDataStore
import com.bookstore.mobile.feature.auth.data.AuthRepository
import com.bookstore.mobile.feature.book.data.BookRepository
import com.bookstore.mobile.feature.cart.data.CartRepository
import com.bookstore.mobile.feature.checkout.data.CheckoutRepository
import com.bookstore.mobile.feature.order.data.OrderRepository
import com.bookstore.mobile.feature.profile.data.ProfileRepository

class AppContainer(context: Context) {
    val tokenDataStore = TokenDataStore(context)
    val settingsDataStore = AppSettingsDataStore(context)
    val apiClient = ApiClient(tokenDataStore, settingsDataStore)

    val authRepository = AuthRepository(apiClient, tokenDataStore)
    val bookRepository = BookRepository(apiClient, settingsDataStore)
    val cartRepository = CartRepository(apiClient)
    val checkoutRepository = CheckoutRepository(apiClient)
    val orderRepository = OrderRepository(apiClient)
    val profileRepository = ProfileRepository(apiClient)
}
