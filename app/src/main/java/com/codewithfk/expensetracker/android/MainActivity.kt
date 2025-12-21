package com.codewithfk.expensetracker.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import com.codewithfk.expensetracker.android.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Observe the theme state from ThemeViewModel and pass it to the app theme
            // provide an initial value while the Flow emits the current preference
            val isDark by themeViewModel.isDarkTheme.collectAsState(initial = false)
            // Force the authored (light) palette and disable dynamic color so runtime matches design
            ExpenseTrackerAndroidTheme(darkTheme = isDark, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHostScreen()
                }
            }
        }
    }
}