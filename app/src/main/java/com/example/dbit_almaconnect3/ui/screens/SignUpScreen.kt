package com.example.dbit_almaconnect3.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dbit_almaconnect3.viewmodel.AuthViewModel

// Admin passcode constant - in a real app, this would be stored securely
private const val ADMIN_PASSCODE = "adminaccess123"

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: (String, String) -> Unit, // (email, role)
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }  // Default role
    var adminPasscode by remember { mutableStateOf("") }  // Add passcode field
    val context = LocalContext.current
    // signUpSuccess is a Pair<Boolean, String> where Boolean indicates success
    // and String holds the role on success or an error message on failure.
    val signUpSuccess by viewModel.signUpSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(signUpSuccess) {
        signUpSuccess?.let { result ->
            if (result.first) {
                onSignUpSuccess(email, result.second)
            } else {
                val errorMsg = if (result.second.isNotEmpty()) result.second else "Sign Up Failed"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
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
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Role selection using radio buttons
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == "student",
                    onClick = { selectedRole = "student" },
                    colors = RadioButtonDefaults.colors()
                )
                Text("Student")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == "alumni",
                    onClick = { selectedRole = "alumni" },
                    colors = RadioButtonDefaults.colors()
                )
                Text("Alumni")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == "collegeadmin",
                    onClick = { selectedRole = "collegeadmin" },
                    colors = RadioButtonDefaults.colors()
                )
                Text("College Admin")
            }
        }
        
        // Show admin passcode field when College Admin is selected
        if (selectedRole == "collegeadmin") {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = adminPasscode,
                onValueChange = { adminPasscode = it },
                label = { Text("Admin Passcode") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    // Validate admin passcode if trying to sign up as an admin
                    if (selectedRole == "collegeadmin" && adminPasscode != ADMIN_PASSCODE) {
                        Toast.makeText(context, "Invalid admin passcode", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.signUp(email, password, selectedRole)
                    }
                } else {
                    Toast.makeText(context, "Enter valid details", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Login")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSignUp() {
    SignUpScreen(onSignUpSuccess = { _, _ -> }, onNavigateToLogin = {})
}
