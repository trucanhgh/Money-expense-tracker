package com.codewithfk.expensetracker.android.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Small container for theme-specific UI values that are not pure colorScheme entries
// so you can easily override UI elements per light/dark themes.
data class AppUiColors(
    val topBarTint: Color,
    val topBarGradientColors: List<Color>,
    val fabIconTint: Color,
    val cardBackground: Color
)

val LocalAppUi = staticCompositionLocalOf<AppUiColors> {
    // default safe fallback (light)
    AppUiColors(topBarTint = Color.Unspecified, topBarGradientColors = listOf(Color.Unspecified, Color.Unspecified), fabIconTint = Color.Unspecified, cardBackground = Color.Unspecified)
}
