package com.bookstore.mobile.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bookstore.mobile.core.ui.ErrorView
import com.bookstore.mobile.core.ui.LoadingView
import com.bookstore.mobile.feature.profile.viewmodel.ProfileViewModel
import com.bookstore.mobile.shared.ui.StoreBackgroundGlow
import com.bookstore.mobile.shared.ui.StoreCard
import com.bookstore.mobile.shared.ui.StoreCardBorder
import com.bookstore.mobile.shared.ui.StoreHeader
import com.bookstore.mobile.shared.ui.StoreIconActionButton
import com.bookstore.mobile.shared.ui.StorePrimaryButton
import com.bookstore.mobile.shared.ui.StoreProfileField
import com.bookstore.mobile.shared.ui.StoreSecondaryButton
import com.bookstore.mobile.shared.ui.StoreSectionTitle
import com.bookstore.mobile.shared.ui.StoreStatusChip
import com.bookstore.mobile.shared.ui.StoreSubText
import com.bookstore.mobile.shared.ui.StoreText
import com.bookstore.mobile.shared.ui.StorefrontBackground
import com.bookstore.mobile.shared.ui.profileFieldIcon

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    when {
        state.isLoading -> LoadingView()
        state.user == null -> ErrorView(
            message = state.errorMessage ?: "Không lấy được tài khoản",
            onRetry = viewModel::load,
        )
        else -> StorefrontBackground {
            StoreBackgroundGlow()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        StoreHeader(
                            title = "Hồ sơ",
                            subtitle = "Quản lý thông tin và tài khoản của bạn",
                            modifier = Modifier.weight(1f),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StoreIconActionButton(
                                icon = Icons.Default.Refresh,
                                onClick = viewModel::load,
                            )
                            StoreIconActionButton(
                                icon = Icons.Default.Settings,
                                onClick = onSettingsClick,
                            )
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StoreCard, RoundedCornerShape(28.dp))
                            .border(1.dp, StoreCardBorder, RoundedCornerShape(28.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProfileAvatar(
                                avatarUrl = state.profile?.avatarUrl,
                                modifier = Modifier.size(96.dp),
                            )
                            Spacer(modifier = Modifier.width(18.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = state.user?.username.orEmpty(),
                                    color = StoreText,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 27.sp,
                                        lineHeight = 31.sp,
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = state.user?.email.orEmpty(),
                                    color = StoreSubText,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            StoreStatusChip(
                                text = "Vai trò: ${state.user?.roles?.firstOrNull().orEmpty()}",
                                tint = Color(0xFFD695FF),
                                background = Color(0xFF281E48),
                                border = Color(0xFF8156BF),
                                icon = Icons.Default.Person,
                                modifier = Modifier.weight(1f),
                            )
                            StoreStatusChip(
                                text = "Trạng thái: ${state.user?.status.orEmpty()}",
                                tint = Color(0xFF8FE6A9),
                                background = Color(0xFF20392C),
                                border = Color(0xFF4B7D57),
                                icon = Icons.Default.Person,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                item {
                    StoreSectionTitle(title = "Thông tin cá nhân")
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StoreCard, RoundedCornerShape(28.dp))
                            .border(1.dp, StoreCardBorder, RoundedCornerShape(28.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        StoreProfileField(
                            label = "Họ",
                            value = state.firstName,
                            onValueChange = viewModel::updateFirstName,
                            icon = profileFieldIcon("Họ"),
                        )
                        StoreProfileField(
                            label = "Tên",
                            value = state.lastName,
                            onValueChange = viewModel::updateLastName,
                            icon = profileFieldIcon("Tên"),
                        )
                        StoreProfileField(
                            label = "Số điện thoại",
                            value = state.phone,
                            onValueChange = viewModel::updatePhone,
                            icon = profileFieldIcon("Số điện thoại"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        )
                    }
                }
                state.successMessage?.let { success ->
                    item {
                        Text(
                            text = success,
                            color = Color(0xFF8FE6A9),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                state.errorMessage?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = Color(0xFFFFA091),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                item {
                    StorePrimaryButton(
                        text = "Lưu hồ sơ",
                        onClick = viewModel::save,
                        icon = Icons.Default.Save,
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    StoreSecondaryButton(
                        text = "Đăng xuất",
                        onClick = { viewModel.logout(onLogout) },
                        icon = Icons.AutoMirrored.Filled.Logout,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0x101E2D58), CircleShape)
            .border(2.dp, StorePurpleGlow, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFD9D3FF),
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

private val StorePurpleGlow = Color(0xFF9A7FFF)
