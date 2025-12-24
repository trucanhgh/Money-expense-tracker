package com.codewithfk.expensetracker.android.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.widget.TopBarWithBack
import kotlinx.coroutines.launch

@Composable
fun SettingsContent(
    rememberedUsername: String?,
    onClearRemember: () -> Unit,
    onNavigateLogin: () -> Unit,
    onBack: () -> Unit = {}
) {
    Scaffold(topBar = {
        TopBarWithBack(
            title = { ExpenseTextView(text = "Cài đặt") },
            onBack = onBack
        )
    }) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(24.dp))
                // Title already present in topBar; keep a section header for clarity
                ExpenseTextView(text = "")
                Spacer(modifier = Modifier.size(24.dp))

                ExpenseTextView(text = "Người dùng đã lưu: ${rememberedUsername ?: "(không)"}")
                Spacer(modifier = Modifier.size(24.dp))

                Button(onClick = {
                    onClearRemember()
                    onNavigateLogin()
                }, modifier = Modifier.fillMaxWidth()) {
                    ExpenseTextView(text = "Đăng xuất")
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val remembered = viewModel.getRememberedUsername()

    SettingsContent(
        rememberedUsername = remembered,
        onClearRemember = {
            scope.launch { viewModel.clearRemember() }
        },
        onNavigateLogin = {
            navController.navigate("/login") {
                popUpTo("/settings") { inclusive = true }
            }
        },
        onBack = { navController.popBackStack() }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewSettingsContent() {
    ExpenseTrackerAndroidTheme {
        SettingsContent(rememberedUsername = "demo_user", onClearRemember = {}, onNavigateLogin = {})
    }
}
