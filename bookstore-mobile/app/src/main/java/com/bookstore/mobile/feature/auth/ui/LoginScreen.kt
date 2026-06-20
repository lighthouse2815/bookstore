package com.bookstore.mobile.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookstore.mobile.feature.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(AuthBackground)),
    ) {
        AuthBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 54.dp, bottom = 28.dp),
        ) {
            AuthBrandMark()
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Bookstore",
                color = AuthTextPrimary,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 56.sp,
                    lineHeight = 58.sp,
                ),
            )
            Text(
                text = "Đăng nhập để mua sách",
                color = AuthTextSecondary,
                style = TextStyle(
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 30.sp,
                ),
                modifier = Modifier.padding(top = 10.dp),
            )

            Spacer(modifier = Modifier.height(78.dp))

            AuthField(
                value = state.username,
                onValueChange = viewModel::updateUsername,
                placeholder = "Email",
                leading = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = AuthAccent,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                placeholder = "Mật khẩu",
                leading = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AuthTextSecondary,
                    )
                },
                trailing = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = "Hiện mật khẩu",
                            tint = AuthTextSecondary,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            )

            TextButton(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Chức năng quên mật khẩu chưa được làm trên mobile demo.")
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp),
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    color = Color(0xFF7A82FF),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            AuthGradientButton(
                text = "Đăng nhập",
                loadingText = "Đang đăng nhập...",
                isLoading = state.isLoading,
                onClick = { viewModel.login(onLoginSuccess) },
                modifier = Modifier.padding(top = 28.dp),
            )

            DividerWithText(
                text = "hoặc",
                modifier = Modifier.padding(top = 34.dp),
            )

            GoogleButton(
                modifier = Modifier.padding(top = 28.dp),
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Google login chưa được nối trên mobile demo.")
                    }
                },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    color = AuthTextSecondary,
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = "Đăng ký",
                        color = Color(0xFF7C72FF),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            TextButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 18.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = AuthTextSecondary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cài đặt API",
                    color = AuthTextSecondary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(18.dp),
        )
    }
}
