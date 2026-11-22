package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.CardType
import com.example.data.PaymentNetwork

// Core Obsidian & Champagne-Emerald Luxury Palette (Dark-first Fintech)
val ObsidianBg = Color(0xFF0B0F17)
val ObsidianSurface = Color(0xFF121824)
val ObsidianSurfaceHigh = Color(0xFF1A2233)
val ObsidianSurfaceElevated = Color(0xFF222C42)
val ObsidianOutline = Color(0xFF2D3A54)
val ObsidianOutlineVariant = Color(0xFF1F293D)

val ChampagneGold = Color(0xFFE6C687)
val ChampagneGoldDark = Color(0xFF3E2F10)
val ChampagneGoldContainer = Color(0xFF352A15)
val ChampagneGoldLight = Color(0xFFF6E6C2)

val EmeraldTeal = Color(0xFF38D39F)
val EmeraldTealDark = Color(0xFF063827)
val EmeraldTealContainer = Color(0xFF11352B)

val SoftCoralError = Color(0xFFFF7A85)
val SoftCoralErrorContainer = Color(0xFF47181E)

val InkWhite = Color(0xFFF3F5FA)
val InkMuted = Color(0xFFA0AEC0)

// Crisp Alabaster & Slate-Navy Light Palette
val AlabasterBg = Color(0xFFF4F6FA)
val AlabasterSurface = Color(0xFFFFFFFF)
val AlabasterSurfaceHigh = Color(0xFFEAEFF7)
val AlabasterSurfaceElevated = Color(0xFFE0E7F3)
val AlabasterOutline = Color(0xFFC3CEE0)
val AlabasterOutlineVariant = Color(0xFFDCE3EF)

val RoyalNavyPrimary = Color(0xFF16284C)
val RoyalNavyContainer = Color(0xFFDCE5F8)
val DeepEmeraldSecondary = Color(0xFF0E7C5B)
val DeepEmeraldContainer = Color(0xFFD2F3E7)
val WarmAmberTertiary = Color(0xFF9C6B16)
val WarmAmberContainer = Color(0xFFFAE9C8)

val SlateInk = Color(0xFF101828)
val SlateMuted = Color(0xFF475467)
val CrimsonError = Color(0xFFC62839)
val CrimsonErrorContainer = Color(0xFFFFE5E8)

// Bespoke metallic card finish palettes per network
data class CardVisualPalette(
    val gradientStart: Color,
    val gradientMid: Color,
    val gradientEnd: Color,
    val accentHighlight: Color,
    val borderGlow: Color,
    val contentColor: Color = Color(0xFFF7F9FC),
    val mutedContentColor: Color = Color(0xCCDAE3F2)
)

fun cardPaletteFor(network: PaymentNetwork, cardType: CardType): CardVisualPalette {
    val isDebit = cardType == CardType.DEBIT
    return when (network) {
        PaymentNetwork.VISA -> if (isDebit) {
            CardVisualPalette(
                gradientStart = Color(0xFF132A4F),
                gradientMid = Color(0xFF1B3F72),
                gradientEnd = Color(0xFF0E203E),
                accentHighlight = Color(0xFF60A5FA),
                borderGlow = Color(0x5593C5FD)
            )
        } else {
            CardVisualPalette(
                gradientStart = Color(0xFF101D3D),
                gradientMid = Color(0xFF1D356A),
                gradientEnd = Color(0xFF0B142B),
                accentHighlight = Color(0xFFE6C687),
                borderGlow = Color(0x55E6C687)
            )
        }

        PaymentNetwork.MASTERCARD -> if (isDebit) {
            CardVisualPalette(
                gradientStart = Color(0xFF241B24),
                gradientMid = Color(0xFF42232B),
                gradientEnd = Color(0xFF19121A),
                accentHighlight = Color(0xFFF59E0B),
                borderGlow = Color(0x55FBBF24)
            )
        } else {
            CardVisualPalette(
                gradientStart = Color(0xFF1A161A),
                gradientMid = Color(0xFF331D22),
                gradientEnd = Color(0xFF120E12),
                accentHighlight = Color(0xFFF97316),
                borderGlow = Color(0x55FB923C)
            )
        }

        PaymentNetwork.RUPAY -> if (isDebit) {
            CardVisualPalette(
                gradientStart = Color(0xFF0E2C2D),
                gradientMid = Color(0xFF134B46),
                gradientEnd = Color(0xFF091F21),
                accentHighlight = Color(0xFF34D399),
                borderGlow = Color(0x556EE7B7)
            )
        } else {
            CardVisualPalette(
                gradientStart = Color(0xFF0A2622),
                gradientMid = Color(0xFF145243),
                gradientEnd = Color(0xFF071B19),
                accentHighlight = Color(0xFFE6C687),
                borderGlow = Color(0x5538D39F)
            )
        }

        PaymentNetwork.AMEX -> if (isDebit) {
            CardVisualPalette(
                gradientStart = Color(0xFF1F2B38),
                gradientMid = Color(0xFF33475B),
                gradientEnd = Color(0xFF16202B),
                accentHighlight = Color(0xFF7DD3FC),
                borderGlow = Color(0x55BAE6FD)
            )
        } else {
            CardVisualPalette(
                gradientStart = Color(0xFF232C36),
                gradientMid = Color(0xFF3D4B59),
                gradientEnd = Color(0xFF181F27),
                accentHighlight = Color(0xFFE2E8F0),
                borderGlow = Color(0x66CBD5E1)
            )
        }

        PaymentNetwork.DISCOVER -> if (isDebit) {
            CardVisualPalette(
                gradientStart = Color(0xFF2E1E14),
                gradientMid = Color(0xFF56341E),
                gradientEnd = Color(0xFF1E130C),
                accentHighlight = Color(0xFFFBBF24),
                borderGlow = Color(0x55FCD34D)
            )
        } else {
            CardVisualPalette(
                gradientStart = Color(0xFF291911),
                gradientMid = Color(0xFF4D2B19),
                gradientEnd = Color(0xFF1A100B),
                accentHighlight = Color(0xFFFDBA74),
                borderGlow = Color(0x55FDBA74)
            )
        }
    }
}
