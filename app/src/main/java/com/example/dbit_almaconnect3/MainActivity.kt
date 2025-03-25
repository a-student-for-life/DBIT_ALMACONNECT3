package com.example.dbit_almaconnect3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.dbit_almaconnect3.navigation.AppNavigator
import com.example.dbit_almaconnect3.ui.theme.DBIT_ALMACONNECT3Theme
import com.example.dbit_almaconnect3.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DBIT_ALMACONNECT3Theme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                AppNavigator(navController, authViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    DBIT_ALMACONNECT3Theme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel()
        AppNavigator(navController, authViewModel)
    }
}
