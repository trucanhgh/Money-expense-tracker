package com.codewithfk.expensetracker.android.ui.theme

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        const val PREFS_NAME = "settings"
        const val KEY_DARK_MODE = "dark_mode"
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Backing MutableStateFlow to expose as a Flow
    private val _isDarkFlow = MutableStateFlow(sharedPrefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkFlow: Flow<Boolean> = _isDarkFlow

    fun setDarkMode(isDark: Boolean) {
        // write to SharedPreferences and update the flow
        sharedPrefs.edit { putBoolean(KEY_DARK_MODE, isDark) }
        _isDarkFlow.value = isDark
    }
}
