package com.codewithfk.expensetracker.android.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(private val prefs: ThemePreferences) : ViewModel() {
    // Expose the stored preference as a Flow
    val isDarkTheme: Flow<Boolean> = prefs.isDarkFlow

    fun toggleTheme() {
        viewModelScope.launch {
            val curr = prefs.isDarkFlow.first()
            prefs.setDarkMode(!curr)
        }
    }
}
