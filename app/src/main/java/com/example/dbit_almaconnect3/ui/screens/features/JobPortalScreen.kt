package com.example.dbit_almaconnect3.ui.screens.features


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun JobPortalScreen(email: String, role: String, navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (role.lowercase() == "alumni") {
            Text(text = "Alumni Job Portal for $email", fontSize = 24.sp)
        } else {
            Text(text = "Student Job & Internship Portal for $email", fontSize = 24.sp)
        }
    }
}