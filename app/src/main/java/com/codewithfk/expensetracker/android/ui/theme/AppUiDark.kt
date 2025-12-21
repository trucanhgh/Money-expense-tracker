package com.codewithfk.expensetracker.android.ui.theme

import androidx.compose.ui.graphics.Color

val AppUiDark = AppUiColors(
    // Use the requested dark topbar color (#394867) for both tint and gradient to match your request
    topBarTint = Color(0xFF394867),
    topBarGradientColors = listOf(Color(0xFF394867), Color(0xFF394867)),
    fabIconTint = ThemeColors.Night.onPrimary,
    cardBackground = ThemeColors.Night.primary
)
