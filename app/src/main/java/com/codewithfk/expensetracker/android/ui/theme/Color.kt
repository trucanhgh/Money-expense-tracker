package com.codewithfk.expensetracker.android.ui.theme

import androidx.compose.ui.graphics.Color

// User-provided palette
val PaletteLightest = Color(0xFFF7F7F7) // #F7F7F7
val PaletteLight = Color(0xFFEEEEEE)    // #EEEEEE
val PaletteDark = Color(0xFF393E46)     // #393E46
val PaletteMuted = Color(0xFF929AAB)    // #929AAB

// Keep Red for expense highlighting
val Red = Color(0xFFFF5252)

// Alias used by UI files
val LightGrey = PaletteMuted

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
        // Use true black for dark theme background/surface as requested
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        // primary in dark mode: slightly lighter than black so surfaces/buttons are visible but still dark
        primary = Color(0xFF1F1F1F),
        secondary = PaletteMuted,
        tertiary = PaletteLight,
        onPrimary = PaletteLightest,    // light text on dark primary
        onBackground = PaletteLightest, // light text on dark background
        onSurface = PaletteLightest
    )

    data object Day : ThemeColors(
        background = PaletteLightest,
        surface = PaletteLight,
        // primary in light mode: dark to contrast with light background
        primary = PaletteDark,
        secondary = PaletteMuted,
        tertiary = PaletteLight,
        onPrimary = PaletteLightest, // light text on dark primary (since primary is dark)
        onBackground = PaletteDark,
        onSurface = PaletteDark
    )
}