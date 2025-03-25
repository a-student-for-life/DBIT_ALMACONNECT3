package com.example.dbit_almaconnect3.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.example.dbit_almaconnect3.ui.screens.HomeScreenWithDrawer
import com.example.dbit_almaconnect3.ui.screens.LoginScreen
import com.example.dbit_almaconnect3.ui.screens.SignUpScreen
import com.example.dbit_almaconnect3.ui.screens.SplashScreen
import com.example.dbit_almaconnect3.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigator(navController: NavHostController, authViewModel: AuthViewModel) {
    // One NavHost at the root now hosts both unauthenticated and authenticated flows.
    NavHost(navController = navController, startDestination = "auth") {
        // Define a nested graph for the unauthenticated flow.
        unauthenticatedNavGraph(navController, authViewModel)

        // Authenticated flow: accept both role and email in the route
        composable("main/{role}/{email}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "student"
            val email = backStackEntry.arguments?.getString("email") ?: ""
            AuthenticatedNavHost(navController, role, email)
        }
    }
}

/**
 * A nested navigation graph for unauthenticated screens.
 */
fun NavGraphBuilder.unauthenticatedNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(startDestination = "splash", route = "auth") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { email, role ->
                    // Navigate to authenticated flow.
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

