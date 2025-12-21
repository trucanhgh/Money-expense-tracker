package com.codewithfk.expensetracker.android.ui.theme

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    // Dark mode removed: always expose false so app stays in light mode
    val isDarkFlow: Flow<Boolean> = flowOf(false)

    // No-op setter to preserve API
    fun setDarkMode(isDark: Boolean) {
        // intentionally no-op
    }
}
