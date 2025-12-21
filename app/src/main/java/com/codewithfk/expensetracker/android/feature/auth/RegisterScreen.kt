package com.codewithfk.expensetracker.android.feature.auth

import android.content.res.Configuration
// dark mode removed; always use light-mode defaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun RegisterContent(
    username: String,
    password: String,
    confirmPassword: String,
    errorMessage: String?,
    successMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCreateAccount: () -> Unit
) {
    val scope = rememberCoroutineScope()
    // dark mode removed; use light defaults
    val isDark = false
    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(24.dp))
                // Use black text in light theme for clearer contrast
                ExpenseTextView(
                    text = "\u0110\u0103ng k\u00fd",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (!isDark) Color.Black else MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { ExpenseTextView(text = "T\u00ean \u0111\u0103ng nh\u1eadp") }
                )

                Spacer(modifier = Modifier.size(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { ExpenseTextView(text = "M\u1eadt kh\u1ea9u (\u00edt nh\u1ea5t 6 k\u00fd t\u1ef1)") }
                )

                Spacer(modifier = Modifier.size(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { ExpenseTextView(text = "X\u00e1c nh\u1eadn m\u1eadt kh\u1ea9u") }
                )

                Spacer(modifier = Modifier.size(16.dp))

                Button(
                    onClick = onCreateAccount,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    ExpenseTextView(text = "T\u1ea1o t\u00e0i kho\u1ea3n")
                }

                Spacer(modifier = Modifier.size(12.dp))

                errorMessage?.let { ExpenseTextView(text = it) }
                successMessage?.let { ExpenseTextView(text = it) }
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    RegisterContent(
        username = username,
        password = password,
        confirmPassword = confirmPassword,
        errorMessage = errorMessage,
        successMessage = successMessage,
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onCreateAccount = {
            // Client-side validation
            errorMessage = null
            successMessage = null
            if (username.isBlank()) {
                errorMessage = "Tên đăng nhập không được để trống"
                return@RegisterContent
            }
            if (password.length < 6) {
                errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                return@RegisterContent
            }
            if (password != confirmPassword) {
                errorMessage = "Mật khẩu không khớp"
                return@RegisterContent
            }

            scope.launch {
                val created = viewModel.registerUser(username.trim(), password)
                if (created) {
                    successMessage = "Tạo tài khoản thành công. Vui lòng đăng nhập."
                    // navigate to login
                    navController.navigate("/login") {
                        popUpTo("/register") { inclusive = true }
                    }
                } else {
                    errorMessage = "Tài khoản đã tồn tại hoặc có lỗi"
                }
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Preview")
@Composable
fun PreviewRegisterContent_Light() {
    ExpenseTrackerAndroidTheme(darkTheme = false, dynamicColor = false) {
        RegisterContent(
            username = "",
            password = "",
            confirmPassword = "",
            errorMessage = null,
            successMessage = null,
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onCreateAccount = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Preview")
@Composable
fun PreviewRegisterContent_Dark() {
    // Dark preview removed; use light theme for consistency
    ExpenseTrackerAndroidTheme(darkTheme = false, dynamicColor = false) {
        RegisterContent(
            username = "",
            password = "",
            confirmPassword = "",
            errorMessage = null,
            successMessage = null,
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onCreateAccount = {}
        )
    }
}
