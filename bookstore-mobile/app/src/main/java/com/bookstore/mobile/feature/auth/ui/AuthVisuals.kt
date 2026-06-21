package com.bookstore.mobile.feature.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val AuthBackground = listOf(
    Color(0xFF06091A),
    Color(0xFF0B1230),
    Color(0xFF17153D),
)
internal val AuthAccent = Color(0xFF7F61FF)
internal val AuthAccentBlue = Color(0xFF4F7EFF)
internal val AuthTextPrimary = Color(0xFFF6F5FA)
internal val AuthTextSecondary = Color(0xFF9E9CB4)
internal val AuthFieldBg = Color(0xFF171D3B).copy(alpha = 0.62f)
internal val AuthFieldBorder = Color.White.copy(alpha = 0.13f)

@Composable
internal fun AuthBrandMark() {
    Box(modifier = Modifier.size(108.dp)) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = Color(0xFFA386FF),
            modifier = Modifier
                .size(86.dp)
                .align(Alignment.BottomStart),
        )
        Text(
            text = "✦",
            color = Color(0xFFE2A7FF),
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        Text(
            text = "✦",
            color = Color(0xFFB890FF),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 18.dp, start = 28.dp),
        )
        Text(
            text = "✦",
            color = Color(0xFFB890FF),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 18.dp),
        )
    }
}

@Composable
internal fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp),
        textStyle = TextStyle(
            color = AuthTextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
        ),
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = AuthTextSecondary,
                fontSize = 19.sp,
            )
        },
        leadingIcon = leading,
        trailingIcon = trailing,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AuthFieldBg,
            unfocusedContainerColor = AuthFieldBg,
            disabledContainerColor = AuthFieldBg,
            focusedBorderColor = AuthFieldBorder,
            unfocusedBorderColor = AuthFieldBorder,
            cursorColor = Color.White,
            focusedTextColor = AuthTextPrimary,
            unfocusedTextColor = AuthTextPrimary,
        ),
    )
}

@Composable
internal fun AuthGradientButton(
    text: String,
    loadingText: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF8848F2), AuthAccentBlue),
                ),
                shape = shape,
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = AuthTextPrimary,
        ),
    ) {
        Text(
            text = if (isLoading) loadingText else text,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
internal fun DividerWithText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.14f),
        )
        Text(
            text = text,
            color = AuthTextSecondary,
            fontSize = 17.sp,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.14f),
        )
    }
}

@Composable
internal fun GoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
            .border(
                width = 1.dp,
                color = AuthFieldBorder,
                shape = RoundedCornerShape(24.dp),
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF101734).copy(alpha = 0.42f),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GoogleGlyph()
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = "Đăng nhập với Google",
                color = AuthTextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun GoogleGlyph() {
    Canvas(modifier = Modifier.size(32.dp)) {
        val stroke = size.minDimension * 0.18f
        val inset = stroke / 2f + 1f
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -35f,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 40f,
            sweepAngle = 92f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 132f,
            sweepAngle = 88f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 220f,
            sweepAngle = 92f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.56f, size.height * 0.5f),
            end = Offset(size.width * 0.9f, size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
internal fun AuthBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF292E91).copy(alpha = 0.22f), Color.Transparent),
                    ),
                    shape = CircleShape,
                )
                .blur(80.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 240.dp, height = 520.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6D4EFF).copy(alpha = 0.16f), Color.Transparent),
                    ),
                    shape = CircleShape,
                )
                .blur(110.dp),
        )
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 16.dp)
                .size(width = 330.dp, height = 360.dp),
        ) {
            val w = size.width
            val h = size.height

            repeat(6) { index ->
                val bookWidth = 22f + index * 2f
                val bookHeight = h * (0.22f + index * 0.045f)
                val x = w * 0.72f + (index % 3) * 30f
                val y = h * 0.23f + (index / 3) * 95f
                drawRoundRect(
                    color = Color(0xFF4A355A).copy(alpha = 0.34f),
                    topLeft = Offset(x, y),
                    size = Size(bookWidth, bookHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                )
            }

            val lampArm = Path().apply {
                moveTo(w * 0.56f, h * 0.12f)
                cubicTo(w * 0.42f, h * 0.12f, w * 0.35f, h * 0.34f, w * 0.34f, h * 0.66f)
            }
            drawPath(
                path = lampArm,
                color = Color(0xFF151A3C),
                style = Stroke(width = 12f, cap = StrokeCap.Round),
            )

            drawOval(
                color = Color(0xFF131834),
                topLeft = Offset(w * 0.45f, h * 0.11f),
                size = Size(w * 0.25f, h * 0.19f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFC77C).copy(alpha = 0.75f), Color.Transparent),
                ),
                radius = w * 0.2f,
                center = Offset(w * 0.67f, h * 0.31f),
            )

            drawRoundRect(
                color = Color(0xFF1F1E47),
                topLeft = Offset(w * 0.44f, h * 0.58f),
                size = Size(w * 0.2f, h * 0.16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
            )
            drawRoundRect(
                color = Color(0xFF2B2350),
                topLeft = Offset(w * 0.41f, h * 0.74f),
                size = Size(w * 0.35f, h * 0.05f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
            )
            drawRoundRect(
                color = Color(0xFF2A234C),
                topLeft = Offset(w * 0.68f, h * 0.72f),
                size = Size(w * 0.28f, h * 0.07f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
            )
            drawRoundRect(
                color = Color(0xFF301D43),
                topLeft = Offset(w * 0.71f, h * 0.63f),
                size = Size(w * 0.16f, h * 0.05f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            )
            drawCircle(
                color = Color(0xFF2D1C39),
                radius = w * 0.045f,
                center = Offset(w * 0.37f, h * 0.7f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF080C1E).copy(alpha = 0.55f)),
                    ),
                ),
        )
    }
}
