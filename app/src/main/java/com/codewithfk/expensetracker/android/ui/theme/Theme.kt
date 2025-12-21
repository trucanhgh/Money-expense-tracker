@file:Suppress("UNUSED_PARAMETER", "unused")

package com.codewithfk.expensetracker.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ThemeColors.Night.primary,
    secondary = ThemeColors.Night.secondary,
    tertiary = ThemeColors.Night.tertiary,
    background = ThemeColors.Night.background,
    surface = ThemeColors.Night.surface,
    onPrimary = ThemeColors.Night.onPrimary,
    onBackground = ThemeColors.Night.onBackground,
    onSurface = ThemeColors.Night.onSurface,
    outline = ThemeColors.Night.secondary
)

private val LightColorScheme = lightColorScheme(
    primary = ThemeColors.Day.primary,
    secondary = ThemeColors.Day.secondary,
    tertiary = ThemeColors.Day.tertiary,
    background = ThemeColors.Day.background,
    surface = ThemeColors.Day.surface,
    onPrimary = ThemeColors.Day.onPrimary,
    onBackground = ThemeColors.Day.onBackground,
    onSurface = ThemeColors.Day.onSurface,
    outline = ThemeColors.Day.secondary
)

@Composable
fun ExpenseTrackerAndroidTheme(
    // Dark mode has been removed; keep a darkTheme parameter for compatibility but ignore it.
    darkTheme: Boolean = false,
     // Dynamic color is available on Android 12+
     // default to false so the app uses the authored palette instead of wallpaper-based dynamic colors
     dynamicColor: Boolean = false,
     content: @Composable () -> Unit
) {
    val colorScheme = when {
        // honor the compatibility darkTheme flag so callers that pass true still get dark colors
        darkTheme -> DarkColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // Only provide dynamic light colors; dark dynamic mode removed
            dynamicLightColorScheme(context)
        }

        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // also set navigation bar color to match the app background so system UI looks consistent
            window.navigationBarColor = colorScheme.background.toArgb()
            // App uses light theme only -> enable dark system icons for status/navigation bars
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    // Provide LocalAppUi and set default LocalContentColor to black so most texts render black by default
    CompositionLocalProvider(
        LocalAppUi provides com.codewithfk.expensetracker.android.ui.theme.appUiColors,
        LocalContentColor provides Color.Black
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}