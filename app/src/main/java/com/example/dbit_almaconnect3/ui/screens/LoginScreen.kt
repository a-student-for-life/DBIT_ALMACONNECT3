package com.example.dbit_almaconnect3.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dbit_almaconnect3.viewmodel.AuthViewModel
import android.net.Uri
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (String, String) -> Unit, // (email, role)
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    // loginSuccess is a Pair<Boolean, String> where the Boolean indicates success, and the String is the role on success.
    val loginSuccess by viewModel.loginSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(loginSuccess) {
        loginSuccess?.let { result ->
            if (result.first) {
                if (email.isNotBlank()) {
                    // Pass email and role to navigation callback.
                    onLoginSuccess(email, result.second)
                } else {
                    Toast.makeText(context, "Email cannot be blank", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (email.isNotBlank() && password.isNotBlank()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(context, "Enter valid credentials", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToSignUp) {
            Text("Don't have an account? Sign Up")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLogin() {
    LoginScreen(onLoginSuccess = { _, _ -> }, onNavigateToSignUp = {})
}
