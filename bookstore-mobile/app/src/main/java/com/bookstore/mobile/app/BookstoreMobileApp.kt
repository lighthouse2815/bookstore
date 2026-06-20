package com.bookstore.mobile.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun BookstoreMobileApp() {
    val context = LocalContext.current.applicationContext
    val container = remember { AppContainer(context) }

    AppTheme {
        AppNavHost(container = container)
    }
}
