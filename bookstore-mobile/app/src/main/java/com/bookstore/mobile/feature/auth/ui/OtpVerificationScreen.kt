package com.bookstore.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.feature.auth.viewmodel.RegisterViewModel
import com.bookstore.mobile.shared.ui.PrimaryButton
import com.bookstore.mobile.shared.ui.TextFieldWithLabel

@Composable
fun OtpVerificationScreen(
    email: String,
    viewModel: RegisterViewModel,
    onVerified: () -> Unit,
    onBackToRegister: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Xac thuc OTP",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = email,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )
        TextFieldWithLabel(
            label = "OTP",
            value = state.otpCode,
            onValueChange = viewModel::updateOtp,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        state.infoMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp))
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
        }
        PrimaryButton(
            text = "Xac thuc",
            isLoading = state.isLoading,
            onClick = { viewModel.verifyOtp(email, onVerified) },
            modifier = Modifier.padding(top = 20.dp),
        )
        TextButton(
            onClick = { viewModel.requestOtp(email) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Gui lai OTP")
        }
        TextButton(
            onClick = onBackToRegister,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Quay lai dang ky")
        }
    }
}
