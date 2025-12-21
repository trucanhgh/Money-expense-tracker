package com.codewithfk.expensetracker.android.ui.theme

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@HiltViewModel
class ThemeViewModel @Inject constructor() : ViewModel() {
    // Dark mode has been removed; always expose light theme
    val isDarkTheme: Flow<Boolean> = flowOf(false)

    // No-op: preserve signature for compatibility
    fun toggleTheme() {
        // intentionally left blank
    }
}
