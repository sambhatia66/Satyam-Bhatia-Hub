package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardType
import com.example.data.PaymentNetwork
import com.example.ui.theme.CardVisualPalette
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.cardPaletteFor

fun Modifier.cardMetallicSurface(
    network: PaymentNetwork,
    cardType: CardType
): Modifier {
    val palette = cardPaletteFor(network, cardType)
    return this.drawBehind {
        // Primary diagonal metallic base gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.gradientStart,
                    palette.gradientMid,
                    palette.gradientEnd
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )

        // Atmospheric radial corner sheen
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.accentHighlight.copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.88f, size.height * 0.14f),
                radius = size.width * 0.65f
            ),
            radius = size.width * 0.65f,
            center = Offset(size.width * 0.88f, size.height * 0.14f)
        )

        // Subtle lower-left glow for depth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.accentHighlight.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.10f, size.height * 0.92f),
                radius = size.width * 0.55f
            ),
            radius = size.width * 0.55f,
            center = Offset(size.width * 0.10f, size.height * 0.92f)
        )

        // Architectural fine metallic arcs
        val strokeColor = Color.White.copy(alpha = 0.06f)
        drawCircle(
            color = strokeColor,
            radius = size.width * 0.42f,
            center = Offset(size.width * 0.96f, size.height * 0.22f),
            style = Stroke(width = 1.2.dp.toPx())
        )
        drawCircle(
            color = strokeColor,
            radius = size.width * 0.54f,
            center = Offset(size.width * 0.96f, size.height * 0.22f),
            style = Stroke(width = 0.9.dp.toPx())
        )
    }
}

@Composable
fun NetworkBadge(
    network: PaymentNetwork,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    when (network) {
        PaymentNetwork.VISA -> {
            Text(
                text = "VISA",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = if (compact) 13.sp else 17.sp,
                letterSpacing = 1.2.sp,
                color = Color(0xFFF8FAFC),
                modifier = modifier
            )
        }

        PaymentNetwork.MASTERCARD -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = modifier
            ) {
                Canvas(
                    modifier = Modifier
                        .width(if (compact) 26.dp else 32.dp)
                        .height(if (compact) 16.dp else 20.dp)
                ) {
                    val r = size.height / 2f
                    drawCircle(
                        color = Color(0xFFEB001B),
                        radius = r,
                        center = Offset(r, r)
                    )
                    drawCircle(
                        color = Color(0xFFF79E1B).copy(alpha = 0.90f),
                        radius = r,
                        center = Offset(size.width - r, r)
                    )
                }
                if (!compact) {
                    Text(
                        text = "Mastercard",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        PaymentNetwork.RUPAY -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = modifier
            ) {
                Text(
                    text = "RuPay",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = if (compact) 13.sp else 16.sp,
                    letterSpacing = 0.3.sp,
                    color = Color(0xFFF8FAFC)
                )
                Canvas(
                    modifier = Modifier.size(if (compact) 12.dp else 15.dp)
                ) {
                    val saffron = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.1f)
                        lineTo(size.width * 0.65f, size.height * 0.5f)
                        lineTo(size.width * 0.15f, size.height * 0.9f)
                        lineTo(size.width * 0.35f, size.height * 0.5f)
                        close()
                    }
                    val green = Path().apply {
                        moveTo(size.width * 0.45f, size.height * 0.1f)
                        lineTo(size.width * 0.95f, size.height * 0.5f)
                        lineTo(size.width * 0.45f, size.height * 0.9f)
                        lineTo(size.width * 0.65f, size.height * 0.5f)
                        close()
                    }
                    drawPath(saffron, color = Color(0xFFFF9933))
                    drawPath(green, color = Color(0xFF138808))
                }
            }
        }

        PaymentNetwork.AMEX -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2E77BB).copy(alpha = 0.85f))
                    .border(
                        width = 0.8.dp,
                        color = Color.White.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = if (compact) 5.dp else 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AMEX",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (compact) 10.sp else 11.sp,
                    letterSpacing = 0.9.sp,
                    color = Color.White
                )
            }
        }

        PaymentNetwork.DISCOVER -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = modifier
            ) {
                Text(
                    text = "DISCOVER",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 10.sp else 12.sp,
                    letterSpacing = 1.0.sp,
                    color = Color(0xFFF8FAFC)
                )
                Canvas(modifier = Modifier.size(if (compact) 7.dp else 9.dp)) {
                    drawCircle(color = Color(0xFFF97316))
                }
            }
        }
    }
}

@Composable
fun MetallicEmvChip(
    palette: CardVisualPalette,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(36.dp)
            .height(26.dp)
    ) {
        val corner = CornerRadius(5.dp.toPx(), 5.dp.toPx())
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF3DFAF),
                    Color(0xFFCFA75E),
                    Color(0xFFEBD29A)
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            size = Size(size.width, size.height),
            cornerRadius = corner
        )
        val lineColor = Color(0xFF5D461E).copy(alpha = 0.55f)
        val stroke = 0.9.dp.toPx()
        // Horizontal chip traces
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height * 0.34f),
            end = Offset(size.width, size.height * 0.34f),
            strokeWidth = stroke
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height * 0.66f),
            end = Offset(size.width, size.height * 0.66f),
            strokeWidth = stroke
        )
        // Vertical chip traces
        drawLine(
            color = lineColor,
            start = Offset(size.width * 0.34f, 0f),
            end = Offset(size.width * 0.34f, size.height),
            strokeWidth = stroke
        )
        drawLine(
            color = lineColor,
            start = Offset(size.width * 0.66f, 0f),
            end = Offset(size.width * 0.66f, size.height),
            strokeWidth = stroke
        )
        // Outer subtle rim
        drawRoundRect(
            color = palette.accentHighlight.copy(alpha = 0.35f),
            size = Size(size.width, size.height),
            cornerRadius = corner,
            style = Stroke(width = 0.8.dp.toPx())
        )
    }
}
