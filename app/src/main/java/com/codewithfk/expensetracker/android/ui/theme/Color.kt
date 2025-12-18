package com.codewithfk.expensetracker.android.ui.theme

import androidx.compose.ui.graphics.Color

// Monochrome palette (black / white / grays)
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Gray900 = Color(0xFF212121)
val Gray800 = Color(0xFF2E2E2E)
val Gray700 = Color(0xFF424242)
val Gray600 = Color(0xFF666666)
val Gray500 = Color(0xFF9E9E9E)
val Gray400 = Color(0xFFBDBDBD)
val Gray300 = Color(0xFFE0E0E0)
val Gray200 = Color(0xFFEEEEEE)
val Gray100 = Color(0xFFF5F5F5)
val Red = Color(0xFFFF5252)
val Green = Color(0xFF4CAF50)

// Alias used by UI files
val LightGrey = Gray400

sealed class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val onPrimary: Color,
    val onBackground: Color,
    val onSurface: Color
) {
    data object Night : ThemeColors(
        background = Black,
        surface = Gray900,
        // primary is a light gray so that interactive elements stand out on the dark background
        primary = Gray300,
        secondary = Gray400,
        tertiary = Gray600,
        onPrimary = Black,      // dark text on light primary
        onBackground = White,   // light text on dark background
        onSurface = White
    )

    data object Day : ThemeColors(
        background = White,
        surface = Gray100,
        // primary is black for strong emphasis in light mode
        primary = Black,
        secondary = Gray700,
        tertiary = Gray500,
        onPrimary = White,      // light text on dark primary
        onBackground = Black,
        onSurface = Black
    )
}