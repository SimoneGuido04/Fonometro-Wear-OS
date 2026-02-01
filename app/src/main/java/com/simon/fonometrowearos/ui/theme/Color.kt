package com.simon.fonometrowearos.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

val NeonGreen = Color(0xFF00FF00)
val NeonAmber = Color(0xFFFFCC00)
val NeonRed = Color(0xFFFF0033)
val DarkBackground = Color(0xFF000000)
val SurfaceGray = Color(0xFF1F1F1F)
val OnSurfaceWhite = Color(0xFFFFFFFF)

val AppColorScheme = ColorScheme(
    primary = NeonGreen,
    onPrimary = Color.Black,
    primaryContainer = SurfaceGray,
    onPrimaryContainer = NeonGreen,
    secondary = NeonAmber,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceGray,
    onSecondaryContainer = NeonAmber,
    tertiary = NeonRed,
    onTertiary = Color.Black,
    tertiaryContainer = SurfaceGray,
    onTertiaryContainer = NeonRed,
    surface = DarkBackground,
    onSurface = OnSurfaceWhite,
    background = DarkBackground,
    onBackground = OnSurfaceWhite,
    error = NeonRed,
    onError = Color.Black
)
