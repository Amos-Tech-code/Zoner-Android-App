package com.zoner.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val LightColorScheme = lightColorScheme(
    primary = ZonerYellow,
    onPrimary = ZonerBlack,
    primaryContainer = Color(0xFFFFF3D0),
    onPrimaryContainer = Color(0xFF271900),

    secondary = GoogleOrange,
    onSecondary = ZonerBlack,

    tertiary = GoogleBlue,
    onTertiary = ZonerWhite,

    background = ZonerWhite,
    onBackground = ZonerBlack,

    surface = ZonerWhite,
    onSurface = ZonerBlack,
    surfaceVariant = ZonerLightGray,
    onSurfaceVariant = ZonerDarkGray,

    error = GoogleRed,
    onError = ZonerWhite,

    outline = ZonerDarkGray
)


private val DarkColorScheme = darkColorScheme(
    primary = ZonerYellow,
    onPrimary = ZonerBlack,
    primaryContainer = Color(0xFF4D3C00),
    onPrimaryContainer = Color(0xFFFFF2CC),

    secondary = GoogleOrange,
    onSecondary = ZonerBlack,

    tertiary = GoogleBlue,
    onTertiary = ZonerWhite,

    background = Color(0xFF121212),
    onBackground = ZonerWhite,

    surface = Color(0xFF1E1E1E),
    onSurface = ZonerWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFAAAAAA),

    error = GoogleRed,
    onError = ZonerBlack,

    outline = Color(0xFFAAAAAA)
)


val DimDarkColorScheme: ColorScheme = darkColorScheme(
    // Brand Colors
    primary = ZonerYellow,
    onPrimary = ZonerBlack,
    primaryContainer = DimColors.PrimaryBluePressed,
    onPrimaryContainer = DimColors.TextPrimary,

    // Secondary Actions (e.g. retweet, success)
    secondary = DimColors.SuccessGreen,
    onSecondary = DimColors.TextPrimary,

    // Tertiary Actions (e.g. reply, links, purple)
    tertiary = DimColors.PurpleReply,
    onTertiary = DimColors.TextPrimary,

    // Backgrounds
    background = DimColors.Background,
    onBackground = DimColors.TextPrimary,

    surface = DimColors.Surface,
    onSurface = DimColors.TextPrimary,
    surfaceVariant = DimColors.ElevatedSurface,
    onSurfaceVariant = DimColors.TextSecondary,

    // Error
    error = DimColors.ErrorRed,
    onError = DimColors.TextPrimary,

    // Outline (used for borders and dividers)
    outline = DimColors.Border
)


@Composable
fun ZonerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DimDarkColorScheme //DarkColorScheme
        else -> LightColorScheme
    }

    // Optional: Set system bar colors
    val view = LocalView.current
    val context = LocalContext.current
    if (!view.isInEditMode) {
        SideEffect {
            (context as? Activity)?.window?.statusBarColor = colorScheme.surface.toArgb()
            ViewCompat.getWindowInsetsController(view)
                ?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZonerTypography,
        content = content
    )
}