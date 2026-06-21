package com.bookstore.mobile.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bookstore.mobile.shared.model.Book
import java.util.Locale

val StoreBackground = Color(0xFF080C1E)
val StoreBackgroundGradient = listOf(
    Color(0xFF050818),
    Color(0xFF0A1030),
    Color(0xFF12183C),
)
val StoreCard = Color(0xFF1A1F40).copy(alpha = 0.76f)
val StoreCardBorder = Color.White.copy(alpha = 0.11f)
val StoreText = Color(0xFFF5F2FA)
val StoreSubText = Color(0xFF9997B5)
val StoreBlue = Color(0xFF62A4FF)
val StorePurple = Color(0xFFA67CFF)

@Composable
fun StorefrontBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(StoreBackgroundGradient)),
        content = content,
    )
}

@Composable
fun StoreBackgroundGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp)
                .size(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3445A9).copy(alpha = 0.20f), Color.Transparent),
                    ),
                    shape = CircleShape,
                )
                .blur(70.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 190.dp, height = 540.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6E57FF).copy(alpha = 0.12f), Color.Transparent),
                    ),
                    shape = CircleShape,
                )
                .blur(100.dp),
        )
    }
}

@Composable
fun StoreHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = StoreText,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 35.sp,
                    lineHeight = 40.sp,
                ),
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = StoreSubText,
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
}

@Composable
fun NotificationBell(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(56.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x101E2D58))
                    .border(1.dp, StoreCardBorder, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = StoreText,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 7.dp, end = 6.dp)
                .size(11.dp)
                .background(Color(0xFFFF8D53), CircleShape),
        )
    }
}

@Composable
fun StoreIconActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(18.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StoreCard, RoundedCornerShape(18.dp))
                .border(1.dp, StoreCardBorder, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StoreText,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
fun StoreSearchBar(
    placeholder: String,
    value: String = "",
    onValueChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        color = StoreCard,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, StoreCardBorder, RoundedCornerShape(22.dp))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = StoreText,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(18.dp))
            if (onValueChange != null) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = StoreText),
                    cursorBrush = SolidColor(StoreText),
                    keyboardOptions = KeyboardOptions.Default,
                    visualTransformation = VisualTransformation.None,
                    decorationBox = { innerTextField ->
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                color = StoreSubText,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    },
                )
            } else {
                Text(
                    text = placeholder,
                    color = StoreSubText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = StoreText,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
fun StoreSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = StoreText,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 30.sp,
            ),
        )
        if (action != null && onActionClick != null) {
            Text(
                text = action,
                color = StorePurple,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable(onClick = onActionClick),
            )
        }
    }
}

data class StoreCategoryStyle(
    val icon: ImageVector,
    val tint: Color,
    val border: Color,
    val glow: Color,
)

fun storeCategoryStyle(name: String): StoreCategoryStyle {
    val normalized = name.lowercase()
    return when {
        "tâm" in normalized || "tam" in normalized -> StoreCategoryStyle(
            icon = Icons.Default.Psychology,
            tint = Color(0xFFD695FF),
            border = Color(0xFF9A61D1),
            glow = Color(0xFF7E48B9).copy(alpha = 0.18f),
        )
        "truyện" in normalized || "truyen" in normalized || "comic" in normalized -> StoreCategoryStyle(
            icon = Icons.Default.Forum,
            tint = Color(0xFF77B6FF),
            border = Color(0xFF487BC5),
            glow = Color(0xFF3C66A5).copy(alpha = 0.18f),
        )
        "văn" in normalized || "van" in normalized || "liter" in normalized -> StoreCategoryStyle(
            icon = Icons.Default.MenuBook,
            tint = Color(0xFF9AF0BA),
            border = Color(0xFF558865),
            glow = Color(0xFF356544).copy(alpha = 0.18f),
        )
        else -> StoreCategoryStyle(
            icon = Icons.Default.NightlightRound,
            tint = Color(0xFFFFA091),
            border = Color(0xFF975B56),
            glow = Color(0xFF6F3D3B).copy(alpha = 0.18f),
        )
    }
}

@Composable
fun StoreCategoryChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val style = storeCategoryStyle(text)
    val border = if (selected) StorePurple else style.border
    val background = if (selected) {
        Brush.linearGradient(listOf(Color(0xFF2E214E), Color(0xFF1E2149)))
    } else {
        Brush.linearGradient(listOf(style.glow, Color.Transparent))
    }
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Surface(
        modifier = modifier.height(54.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = clickableModifier
                .background(background, RoundedCornerShape(18.dp))
                .border(
                    width = if (selected) 1.4.dp else 1.dp,
                    color = border,
                    shape = RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showIcon) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = if (selected) Color(0xFFE6D7FF) else style.tint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = text,
                color = if (selected) Color.White else style.tint,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun StoreBookCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = StoreCard,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, StoreCardBorder, RoundedCornerShape(26.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            if (!book.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = book.title,
                    modifier = Modifier
                        .width(98.dp)
                        .height(148.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(98.dp)
                        .height(148.dp)
                        .background(Color(0xFF22274B), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = StoreSubText,
                        modifier = Modifier.size(42.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            color = StoreText,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 23.sp,
                                lineHeight = 29.sp,
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = book.author.ifBlank { "Chưa rõ tác giả" },
                            color = StoreSubText,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color(0xFFD4D1E3),
                        modifier = Modifier.size(34.dp),
                    )
                }

                Text(
                    text = formatPriceValue(book.price),
                    color = StoreBlue,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 23.sp,
                    ),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = StoreSubText,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Tồn: ${book.stockQuantity}",
                        color = StoreSubText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "|",
                        color = StoreSubText.copy(alpha = 0.5f),
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF7C14E),
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", book.rating ?: 0.0),
                        color = StoreSubText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun StoreStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.MenuBook,
    tint: Color,
    background: Color,
    border: Color,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            color = tint,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun StorePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
) {
    val enabled = !isLoading
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF8C47F6), Color(0xFF4A79FF)),
                    ),
                    shape = RoundedCornerShape(24.dp),
                )
                .border(1.dp, Color(0xFFAB92FF), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                icon != null -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                }
            }
            if (!isLoading) {
                Text(
                    text = text,
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                    ),
                )
            }
        }
    }
}

@Composable
fun StoreSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = StorePurple,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent, RoundedCornerShape(24.dp))
                .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
            }
            Text(
                text = text,
                color = accent,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp,
                ),
            )
        }
    }
}

@Composable
fun StoreProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Person,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(color = StoreText),
        label = {
            Text(
                text = label,
                color = StoreSubText,
                style = MaterialTheme.typography.titleSmall,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StoreSubText,
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = StoreSubText,
            )
        },
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = StoreText,
            unfocusedTextColor = StoreText,
            cursorColor = StorePurple,
            focusedBorderColor = StorePurple.copy(alpha = 0.75f),
            unfocusedBorderColor = StoreCardBorder,
            focusedLabelColor = StoreSubText,
            unfocusedLabelColor = StoreSubText,
            focusedLeadingIconColor = StoreSubText,
            unfocusedLeadingIconColor = StoreSubText,
            focusedTrailingIconColor = StoreSubText,
            unfocusedTrailingIconColor = StoreSubText,
            focusedContainerColor = StoreCard,
            unfocusedContainerColor = StoreCard,
        ),
        shape = RoundedCornerShape(22.dp),
    )
}

@Composable
fun OrdersEmptyIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(320.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color(0xFF171E46).copy(alpha = 0.46f), CircleShape)
                .border(1.dp, Color(0xFF2B346D).copy(alpha = 0.8f), CircleShape),
        )

        val starColor = Color(0xFFFFC67A)
        listOf(
            Triple((-116).dp, (-52).dp, 4.dp),
            Triple(112.dp, (-86).dp, 4.dp),
            Triple((-88).dp, 42.dp, 3.dp),
            Triple(138.dp, 8.dp, 5.dp),
            Triple(88.dp, 84.dp, 3.dp),
        ).forEach { (x, y, size) ->
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(size)
                    .background(starColor, CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-34).dp),
        ) {
            Box(
                modifier = Modifier
                    .offset(y = 62.dp)
                    .width(188.dp)
                    .height(28.dp)
                    .background(Color(0xFF251D3C), RoundedCornerShape(14.dp)),
            )
            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = 46.dp)
                    .width(170.dp)
                    .height(26.dp)
                    .background(Color(0xFF463057), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF6D587D), RoundedCornerShape(12.dp)),
            )
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 26.dp)
                    .width(182.dp)
                    .height(32.dp)
                    .background(Color(0xFF23345F), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF4D5C8C), RoundedCornerShape(12.dp)),
            )
            Box(
                modifier = Modifier
                    .offset(x = 36.dp, y = (-8).dp)
                    .width(126.dp)
                    .height(100.dp)
                    .background(Color(0xFFC99158), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFE2BC88), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF6E4830),
                    modifier = Modifier.size(34.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 8.dp)
                        .width(64.dp)
                        .height(10.dp)
                        .background(Color(0xFF7C625D), RoundedCornerShape(4.dp)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-12).dp)
                        .width(12.dp)
                        .height(72.dp)
                        .background(Color(0xFFE7BE84), RoundedCornerShape(6.dp)),
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = 102.dp, y = 30.dp)
                    .width(58.dp)
                    .height(120.dp)
                    .rotate(11f)
                    .background(Color(0xFFF0DCC4), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE3C4A7), RoundedCornerShape(12.dp)),
            ) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .offset(x = 12.dp, y = (20 + index * 16).dp)
                            .width(if (index == 4) 20.dp else 30.dp)
                            .height(3.dp)
                            .background(Color(0xFFD0B299), RoundedCornerShape(2.dp)),
                    )
                }
                Icon(
                    imageVector = Icons.Default.ShoppingCartCheckout,
                    contentDescription = null,
                    tint = Color(0xFF987153),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                        .size(24.dp),
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = (-84).dp, y = 30.dp)
                    .width(42.dp)
                    .height(90.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(32.dp)
                        .height(56.dp)
                        .background(Color(0xFF3A2D3D), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF715D73), RoundedCornerShape(16.dp)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-20).dp)
                        .size(18.dp)
                        .background(Color(0xFFFFC97E), CircleShape)
                        .blur(3.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-14).dp)
                        .width(10.dp)
                        .height(18.dp)
                        .background(Color(0xFFFFE6A9), RoundedCornerShape(12.dp)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(18.dp)
                        .height(10.dp)
                        .border(2.dp, Color(0xFF715D73), RoundedCornerShape(8.dp)),
                )
            }
        }
    }
}

fun profileFieldIcon(label: String): ImageVector {
    return when {
        label.contains("điện thoại", ignoreCase = true) -> Icons.Default.Call
        else -> Icons.Default.Person
    }
}

private fun formatPriceValue(price: Double): String {
    return String.format(Locale.US, "%,d đ", price.toLong()).replace(',', '.')
}
