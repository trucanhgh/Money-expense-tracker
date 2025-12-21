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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import kotlinx.coroutines.launch

@Composable
fun SettingsContent(
    rememberedUsername: String?,
    onClearRemember: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(24.dp))
                ExpenseTextView(text = "Cài đặt")
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
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsContent() {
    ExpenseTrackerAndroidTheme {
        SettingsContent(rememberedUsername = "demo_user", onClearRemember = {}, onNavigateLogin = {})
    }
}
