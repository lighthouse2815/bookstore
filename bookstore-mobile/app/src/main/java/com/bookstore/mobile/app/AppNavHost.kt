package com.bookstore.mobile.app

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.core.util.AppViewModelFactory
import com.bookstore.mobile.feature.auth.ui.LoginScreen
import com.bookstore.mobile.feature.auth.ui.OtpVerificationScreen
import com.bookstore.mobile.feature.auth.ui.RegisterScreen
import com.bookstore.mobile.feature.auth.viewmodel.AuthViewModel
import com.bookstore.mobile.feature.auth.viewmodel.RegisterViewModel
import com.bookstore.mobile.feature.book.ui.BookDetailScreen
import com.bookstore.mobile.feature.book.ui.BookListScreen
import com.bookstore.mobile.feature.book.viewmodel.BookDetailViewModel
import com.bookstore.mobile.feature.book.viewmodel.BookListViewModel
import com.bookstore.mobile.feature.cart.ui.CartScreen
import com.bookstore.mobile.feature.cart.viewmodel.CartViewModel
import com.bookstore.mobile.feature.checkout.ui.CheckoutScreen
import com.bookstore.mobile.feature.checkout.ui.OrderSuccessScreen
import com.bookstore.mobile.feature.checkout.viewmodel.CheckoutViewModel
import com.bookstore.mobile.feature.home.ui.HomeScreen
import com.bookstore.mobile.feature.home.viewmodel.HomeViewModel
import com.bookstore.mobile.feature.order.ui.OrderDetailScreen
import com.bookstore.mobile.feature.order.ui.OrderListScreen
import com.bookstore.mobile.feature.order.viewmodel.OrderDetailViewModel
import com.bookstore.mobile.feature.order.viewmodel.OrderListViewModel
import com.bookstore.mobile.feature.profile.ui.ProfileScreen
import com.bookstore.mobile.feature.profile.ui.SettingsScreen
import com.bookstore.mobile.feature.profile.viewmodel.ProfileViewModel
import com.bookstore.mobile.feature.profile.viewmodel.SettingsViewModel
import com.bookstore.mobile.shared.ui.BottomNavBar
import com.bookstore.mobile.shared.ui.StoreBackground

@Composable
fun AppNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = StoreBackground,
        bottomBar = {
            if (currentRoute in BottomRoutes) {
                BottomNavBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(AppRoute.Home.route) {
                            saveState = true
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppRoute.Splash.route) {
                val viewModel: AuthViewModel = viewModel(
                    factory = AppViewModelFactory { AuthViewModel(container.authRepository) },
                )
                LaunchedEffect(Unit) {
                    viewModel.checkSession(
                        onAuthenticated = {
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        },
                        onUnauthenticated = {
                            navController.navigate(AppRoute.Login.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        },
                    )
                }
                LoadingView("Đang kiểm tra phiên đăng nhập...")
            }

            composable(AppRoute.Login.route) {
                val viewModel: AuthViewModel = viewModel(
                    factory = AppViewModelFactory { AuthViewModel(container.authRepository) },
                )
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate(AppRoute.Register.route) },
                    onSettingsClick = { navController.navigate(AppRoute.Settings.route) },
                )
            }

            composable(AppRoute.Register.route) {
                val viewModel: RegisterViewModel = viewModel(
                    factory = AppViewModelFactory { RegisterViewModel(container.authRepository) },
                )
                RegisterScreen(
                    viewModel = viewModel,
                    onOtpRequired = { email -> navController.navigate(AppRoute.OtpVerification.create(email)) },
                    onLoginClick = { navController.popBackStack(AppRoute.Login.route, inclusive = false) },
                )
            }

            composable(
                route = AppRoute.OtpVerification.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType }),
            ) { entry ->
                val email = Uri.decode(entry.arguments?.getString("email").orEmpty())
                val viewModel: RegisterViewModel = viewModel(
                    factory = AppViewModelFactory { RegisterViewModel(container.authRepository) },
                )
                OtpVerificationScreen(
                    email = email,
                    viewModel = viewModel,
                    onVerified = {
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.Register.route) { inclusive = true }
                        }
                    },
                    onBackToRegister = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Home.route) {
                val viewModel: HomeViewModel = viewModel(
                    factory = AppViewModelFactory {
                        HomeViewModel(container.authRepository, container.bookRepository)
                    },
                )
                HomeScreen(
                    viewModel = viewModel,
                    onBookClick = { navController.navigate(AppRoute.BookDetail.create(it)) },
                    onBooksClick = { navController.navigate(AppRoute.BookList.route) },
                )
            }

            composable(AppRoute.BookList.route) {
                val viewModel: BookListViewModel = viewModel(
                    factory = AppViewModelFactory { BookListViewModel(container.bookRepository) },
                )
                BookListScreen(
                    viewModel = viewModel,
                    onBookClick = { navController.navigate(AppRoute.BookDetail.create(it)) },
                )
            }

            composable(
                route = AppRoute.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
            ) { entry ->
                val bookId = entry.arguments?.getString("bookId").orEmpty()
                val viewModel: BookDetailViewModel = viewModel(
                    key = "book-detail-$bookId",
                    factory = AppViewModelFactory {
                        BookDetailViewModel(container.bookRepository, container.cartRepository)
                    },
                )
                BookDetailScreen(
                    bookId = bookId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLoginRequired = { navController.navigate(AppRoute.Login.route) },
                )
            }

            composable(AppRoute.Cart.route) {
                val viewModel: CartViewModel = viewModel(
                    factory = AppViewModelFactory { CartViewModel(container.cartRepository) },
                )
                CartScreen(
                    viewModel = viewModel,
                    onCheckout = { itemIds ->
                        navController.navigate(AppRoute.Checkout.create(itemIds))
                    },
                )
            }

            composable(
                route = AppRoute.Checkout.route,
                arguments = listOf(
                    navArgument("itemIds") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { entry ->
                val selectedCartItemIds = Uri.decode(entry.arguments?.getString("itemIds").orEmpty())
                    .split(",")
                    .map(String::trim)
                    .filter(String::isNotBlank)
                val viewModel: CheckoutViewModel = viewModel(
                    factory = AppViewModelFactory { CheckoutViewModel(container.checkoutRepository) },
                )
                CheckoutScreen(
                    selectedCartItemIds = selectedCartItemIds,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { result ->
                        navController.navigate(
                            AppRoute.OrderSuccess.create(
                                orderId = result.orderId,
                                orderCode = result.orderCode,
                                totalAmount = result.totalAmount,
                                paymentMethod = result.paymentMethod,
                                paymentStatus = result.paymentStatus,
                                orderStatus = result.orderStatus,
                                transferContent = result.transferContent,
                            ),
                        )
                    },
                )
            }

            composable(
                route = AppRoute.OrderSuccess.route,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("orderCode") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("totalAmount") {
                        type = NavType.FloatType
                        defaultValue = 0f
                    },
                    navArgument("paymentMethod") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("paymentStatus") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("orderStatus") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("transferContent") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { entry ->
                val orderId = Uri.decode(entry.arguments?.getString("orderId").orEmpty())
                val orderCode = Uri.decode(entry.arguments?.getString("orderCode").orEmpty())
                val totalAmount = entry.arguments?.getFloat("totalAmount")?.toDouble() ?: 0.0
                val paymentMethod = Uri.decode(entry.arguments?.getString("paymentMethod").orEmpty())
                val paymentStatus = Uri.decode(entry.arguments?.getString("paymentStatus").orEmpty())
                val orderStatus = Uri.decode(entry.arguments?.getString("orderStatus").orEmpty())
                val transferContent = Uri.decode(entry.arguments?.getString("transferContent").orEmpty())
                OrderSuccessScreen(
                    orderId = orderId,
                    orderCode = orderCode,
                    totalAmount = totalAmount,
                    paymentMethod = paymentMethod,
                    paymentStatus = paymentStatus,
                    orderStatus = orderStatus,
                    transferContent = transferContent,
                    onOrdersClick = {
                        navController.navigate(AppRoute.Orders.route) {
                            popUpTo(AppRoute.Home.route)
                        }
                    },
                    onHomeClick = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Home.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(AppRoute.Orders.route) {
                val viewModel: OrderListViewModel = viewModel(
                    factory = AppViewModelFactory { OrderListViewModel(container.orderRepository) },
                )
                OrderListScreen(
                    viewModel = viewModel,
                    onOrderClick = { navController.navigate(AppRoute.OrderDetail.create(it)) },
                    onShopNow = { navController.navigate(AppRoute.BookList.route) },
                    onFeaturedBooks = { navController.navigate(AppRoute.Home.route) },
                )
            }

            composable(
                route = AppRoute.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { entry ->
                val orderId = entry.arguments?.getString("orderId").orEmpty()
                val viewModel: OrderDetailViewModel = viewModel(
                    key = "order-detail-$orderId",
                    factory = AppViewModelFactory { OrderDetailViewModel(container.orderRepository) },
                )
                OrderDetailScreen(
                    orderId = orderId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = AppViewModelFactory {
                        ProfileViewModel(container.profileRepository, container.authRepository)
                    },
                )
                ProfileScreen(
                    viewModel = viewModel,
                    onSettingsClick = { navController.navigate(AppRoute.Settings.route) },
                    onLogout = {
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(0)
                        }
                    },
                )
            }

            composable(AppRoute.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = AppViewModelFactory {
                        SettingsViewModel(container.settingsDataStore, container.apiClient)
                    },
                )
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
