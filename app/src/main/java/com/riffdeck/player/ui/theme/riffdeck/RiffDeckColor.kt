package com.riffdeck.player.ui.theme.riffdeck

import androidx.compose.ui.graphics.Color

/**
 * RiffDeck palette — brushed steel, chrome, jewel-case gloss, and neon
 * accent colors inspired by early-2000s rock/metal hardware (amps, iPods,
 * car stereos, CD players) rather than flat Material Design.
 */
object RiffDeckColor {

    // --- Metal base ---
    val GunmetalDark = Color(0xFF15171B)
    val GunmetalBase = Color(0xFF1E2126)
    val GunmetalMid = Color(0xFF2A2E35)
    val BrushedSteel = Color(0xFF3B4048)
    val BrushedSteelLight = Color(0xFF565D68)
    val ChromeHighlight = Color(0xFFAEB6C2)
    val ChromeSpecular = Color(0xFFEFF3F8)

    // --- Rivets / bevels / shadow-carved edges ---
    val RivetShadow = Color(0xCC000000)
    val RivetHighlight = Color(0x33FFFFFF)
    val BevelInsetDark = Color(0x66000000)
    val BevelInsetLight = Color(0x1FFFFFFF)

    // --- Neon / LCD accents (Y2K) ---
    val NeonRed = Color(0xFFFF1E3C)
    val NeonRedGlow = Color(0xFFFF5A6E)
    val NeonOrange = Color(0xFFFF8A1E)
    val AmpAmber = Color(0xFFFFB020)
    val LcdGreen = Color(0xFF4CFFB0)
    val LcdGreenDim = Color(0xFF1F7A56)
    val PlasmaPurple = Color(0xFFB24CFF)
    val ToxicLime = Color(0xFFCFFF3C)

    // --- Leather / jewel-case gloss (skeuomorphic textures) ---
    val LeatherBrown = Color(0xFF2B1B18)
    val LeatherStitch = Color(0xFF4A332C)
    val JewelGlossTop = Color(0x59FFFFFF)
    val JewelGlossBottom = Color(0x00FFFFFF)

    // --- Surfaces ---
    val SurfaceDeep = GunmetalDark
    val SurfacePanel = GunmetalBase
    val SurfaceRaised = GunmetalMid
    val SurfaceInset = Color(0xFF101215)

    // --- Text ---
    val TextPrimary = Color(0xFFF2F3F5)
    val TextSecondary = Color(0xFFA7ADB8)
    val TextMuted = Color(0xFF6B7280)
    val TextOnNeon = Color(0xFF0A0A0A)

    // --- Semantic ---
    val Danger = NeonRed
    val Success = LcdGreen
    val AccentPrimary = NeonRed
    val AccentSecondary = AmpAmber
}
