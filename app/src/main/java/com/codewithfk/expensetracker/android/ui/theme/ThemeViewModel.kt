package com.codewithfk.expensetracker.android.ui.theme

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@HiltViewModel
class ThemeViewModel @Inject constructor(private val prefs: ThemePreferences) : ViewModel() {
    // Dark mode removed: always expose light theme
    val isDarkTheme: Flow<Boolean> = flowOf(false)

    // No-op toggle to keep API compatibility
    fun toggleTheme() {
        // intentionally no-op
    }
}
