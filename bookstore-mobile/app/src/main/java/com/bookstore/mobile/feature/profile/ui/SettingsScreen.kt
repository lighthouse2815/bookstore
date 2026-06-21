package com.bookstore.mobile.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.core.ui.AppTopBar
import com.bookstore.mobile.feature.profile.viewmodel.SettingsViewModel
import com.bookstore.mobile.shared.ui.PrimaryButton
import com.bookstore.mobile.shared.ui.TextFieldWithLabel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { AppTopBar(title = "Settings", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextFieldWithLabel(
                label = "API base URL",
                value = state.baseUrl,
                onValueChange = viewModel::updateBaseUrl,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Mặc định hiện tại: http://192.168.1.10:8080")
            Text("Emulator: http://10.0.2.2:8080")
            Text("Máy thật: http://192.168.1.10:8080")
            state.successMessage?.let { Text(it) }
            PrimaryButton(
                text = "Lưu",
                isLoading = false,
                onClick = viewModel::save,
            )
            OutlinedButton(
                onClick = viewModel::resetDefault,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Đặt về mặc định")
            }
        }
    }
}
