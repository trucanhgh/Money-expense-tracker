package com.codewithfk.expensetracker.android.feature.auth

// dark mode support removed; always use light-mode defaults
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
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
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

// Add a top-level LoginState sealed class (file-scope) to avoid illegal local sealed/object declarations
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

@Composable
fun LoginContent(
    username: String,
    password: String,
    remember: Boolean,
    showMessage: String?,
    isLoading: Boolean = false,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    // dark mode removed; always light
    val isDark = false

    // adaptive text color: white in dark mode, black in light mode
    val adaptiveTextColor = if (isDark) Color.White else Color.Black
    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(24.dp))
                ExpenseTextView(
                    text = "Ch\u00e0o m\u1eebng",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (!isDark) Color.Black else MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(24.dp))

                OutlinedTextField(value = username, onValueChange = onUsernameChange, modifier = Modifier.fillMaxWidth(), placeholder = { ExpenseTextView(text = "T\u00ean \u0111\u0103ng nh\u1eadp") })
                Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { ExpenseTextView(text = "M\u1eadt kh\u1ea9u") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        // Use a simple clickable text toggle to avoid depending on material icon artifacts in this project.
                        ExpenseTextView(
                            text = if (passwordVisible) "\u1ea8n" else "Hi\u1ec7n",
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )
                Spacer(modifier = Modifier.size(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = remember, onCheckedChange = onRememberChange)
                    Spacer(modifier = Modifier.size(8.dp))
                    ExpenseTextView(text = "Ghi nh\u1edb \u0111\u0103ng nh\u1eadp", color = adaptiveTextColor)
                }
                Spacer(modifier = Modifier.size(24.dp))
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = androidx.compose.ui.unit.Dp.Hairline, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        ExpenseTextView(text = "\u0110\u0103ng nh\u1eadp")
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))
                showMessage?.let { msg ->
                    ExpenseTextView(text = msg)
                }

                // Small '\u0110\u0103ng k\u00fd' link at the bottom-right
                Spacer(modifier = Modifier.size(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ExpenseTextView(text = "\u0110\u0103ng k\u00fd", color = adaptiveTextColor, modifier = Modifier.clickable { onNavigateRegister() })
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

    // Use the file-level LoginState
    var loginState by remember { mutableStateOf<LoginState>(LoginState.Idle) }

    // If already remembered, navigate to home directly (kept as a structured step)
    LaunchedEffect(Unit) {
        val remembered = viewModel.getRememberedUsername()
        if (!remembered.isNullOrBlank()) {
            // also set current user for this session
            viewModel.saveCurrentUser(remembered)
            // direct navigation as success case
            navController.navigate("/home") {
                popUpTo("/login") { inclusive = true }
            }
        }
    }

    // Effect: react to Success state once it appears
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            navController.navigate("/home") {
                popUpTo("/login") { inclusive = true }
            }
        }
    }

    // Provide the LoginContent and wire actions into the state machine
    LoginContent(
        username = username,
        password = password,
        remember = remember,
        showMessage = (loginState as? LoginState.Error)?.message,
        isLoading = loginState is LoginState.Loading,
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onRememberChange = { remember = it },
        onLoginClick = {
            // structured login flow: set Loading, attempt login, then set Success/Error
            scope.launch {
                loginState = LoginState.Loading
                try {
                    val ok = viewModel.loginUser(username.trim(), password)
                    if (ok) {
                        viewModel.saveCurrentUser(username.trim())
                        if (remember) viewModel.saveRememberUsername(username.trim())
                        loginState = LoginState.Success
                    } else {
                        loginState = LoginState.Error("Tên đăng nhập hoặc mật khẩu không đúng")
                    }
                } catch (t: Throwable) {
                    loginState = LoginState.Error(t.message ?: "Lỗi khi kết nối")
                }
            }
        },
        onNavigateRegister = { navController.navigate("/register") }
    )
}

@Preview(showBackground = true, name = "Light Preview")
@Composable
fun PreviewLoginContent_Light() {
    ExpenseTrackerAndroidTheme(darkTheme = false, dynamicColor = false) {
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
}

// Dark preview removed; use light theme for consistency
@Preview(showBackground = true, uiMode = 32, name = "Dark Preview")
@Composable
fun PreviewLoginContent_Dark() {
    ExpenseTrackerAndroidTheme(darkTheme = false, dynamicColor = false) {
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
}
