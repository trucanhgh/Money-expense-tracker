package com.codewithfk.expensetracker.android.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun LoginContent(
    username: String,
    password: String,
    remember: Boolean,
    showMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(24.dp))
                ExpenseTextView(text = "Chào mừng", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.size(24.dp))

                OutlinedTextField(value = username, onValueChange = onUsernameChange, modifier = Modifier.fillMaxWidth(), placeholder = { ExpenseTextView(text = "Tên đăng nhập") })
                Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { ExpenseTextView(text = "Mật khẩu") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        // Use a simple clickable text toggle to avoid depending on material icon artifacts in this project.
                        ExpenseTextView(
                            text = if (passwordVisible) "Ẩn" else "Hiện",
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )
                Spacer(modifier = Modifier.size(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = remember, onCheckedChange = onRememberChange)
                    Spacer(modifier = Modifier.size(8.dp))
                    ExpenseTextView(text = "Ghi nhớ đăng nhập")
                }
                Spacer(modifier = Modifier.size(24.dp))
                Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                    ExpenseTextView(text = "Đăng nhập")
                }

                Spacer(modifier = Modifier.size(12.dp))
                showMessage?.let { msg ->
                    ExpenseTextView(text = msg)
                }

                // Small 'Đăng ký' link at the bottom-right
                Spacer(modifier = Modifier.size(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ExpenseTextView(text = "Đăng ký", modifier = Modifier.clickable { onNavigateRegister() })
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var remember by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }

    // If already remembered, navigate to home directly
    LaunchedEffect(Unit) {
        val remembered = viewModel.getRememberedUsername()
        if (!remembered.isNullOrBlank()) {
            // also set current user for this session
            viewModel.saveCurrentUser(remembered)
            navController.navigate("/home") {
                popUpTo("/login") { inclusive = true }
            }
        }
    }

    LoginContent(
        username = username,
        password = password,
        remember = remember,
        showMessage = showMessage,
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onRememberChange = { remember = it },
        onLoginClick = {
            scope.launch {
                val ok = viewModel.loginUser(username.trim(), password)
                if (ok) {
                    // save session and optionally remember
                    viewModel.saveCurrentUser(username.trim())
                    if (remember) viewModel.saveRememberUsername(username.trim())
                    navController.navigate("/home") {
                        popUpTo("/login") { inclusive = true }
                    }
                } else {
                    showMessage = "Tên đăng nhập hoặc mật khẩu không đúng"
                }
            }
        },
        onNavigateRegister = { navController.navigate("/register") }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginContent() {
    LoginContent(
        username = "",
        password = "",
        remember = false,
        showMessage = null,
        onUsernameChange = {},
        onPasswordChange = {},
        onRememberChange = {},
        onLoginClick = {},
        onNavigateRegister = {}
    )
}
