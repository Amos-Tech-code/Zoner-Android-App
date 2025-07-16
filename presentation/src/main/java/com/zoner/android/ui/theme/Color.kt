package com.zoner.android.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Color
val ZonerYellow = Color(0xFFFAB631)

// Brand Colors
val ZonerDarkGray = Color(0xFF6D6D6D)
val ZonerLightGray = Color(0xFFF5F5F5)
val ZonerBlack = Color(0xFF000000)
val ZonerWhite = Color(0xFFFFFFFF)

// Google Inspired
val GoogleBlue = Color(0xFF1976D2)
val GoogleGreen = Color(0xFF4CAF50)
val GoogleRed = Color(0xFFFF3D00)
val GoogleOrange = Color(0xFFFFC107)

// App Status Colors
val Info = GoogleBlue
val Success = GoogleGreen
val Warning = GoogleOrange
val Error = GoogleRed


/**
 * Dim Dark Mode Color Palette
 */
object DimColors {
    // Backgrounds
    val Background = Color(0xFF15202B)
    val Surface = Color(0xFF192734)
    val ElevatedSurface = Color(0xFF203344) // Used for floating elements

    // Text
    val TextPrimary = Color(0xFFE7E9EA)
    val TextSecondary = Color(0xFF8B98A5)
    val TextDisabled = Color(0xFF5A6D7E)

    // Accents
    val PrimaryBlue = Color(0xFF1D9BF0)
    val PrimaryBluePressed = Color(0xFF1A8CD8)
    val ErrorRed = Color(0xFFF91880) // Used for likes/errors
    val SuccessGreen = Color(0xFF00BA7C) // Used for retweets
    val WarningYellow = Color(0xFFFFD400) // Used for gold badges

    // Borders & Dividers
    val Divider = Color(0xFF38444D)
    val Border = Color(0xFF2F3D49)

    // Interactive States
    val HoverOverlay = Color(0x15FFFFFF)
    val PressedOverlay = Color(0x25FFFFFF)
    val DisabledOverlay = Color(0x60FFFFFF)

    // Custom X features
    val VerifiedBlue = Color(0xFF1D9BF0)
    val GoldBadge = Color(0xFFFFD400)
    val SilverBadge = Color(0xFFE7E9EA)
    val PurpleReply = Color(0xFF7856FF)
}
