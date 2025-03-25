package com.example.dbit_almaconnect3.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Updated imports to the correct package.
import com.example.dbit_almaconnect3.ui.screens.LoginScreen
import com.example.dbit_almaconnect3.ui.screens.SignUpScreen
import com.example.dbit_almaconnect3.ui.screens.SplashScreen
import com.example.dbit_almaconnect3.viewmodel.AuthViewModel

@Composable
fun UnauthenticatedNavHost(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { email, role ->
                    navController.navigate("main/${Uri.encode(role)}/${Uri.encode(email)}") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { email, role ->
                    navController.navigate("main/${Uri.encode(role)}/${Uri.encode(email)}") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun UnauthenticatedNavHostPreview() {
    // Create a dummy ViewModelStoreOwner for preview purposes.
    val dummyOwner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides dummyOwner) {
        val navController = rememberNavController()
        // Pass an instance of AuthViewModel (or a dummy version if needed).
        UnauthenticatedNavHost(navController, AuthViewModel())
    }
}