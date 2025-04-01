package com.example.dbit_almaconnect3.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dbit_almaconnect3.ui.screens.DiscussionScreen
import com.example.dbit_almaconnect3.ui.screens.HomeScreenWithDrawer
import com.example.dbit_almaconnect3.ui.screens.DrawerContent
import com.example.dbit_almaconnect3.ui.screens.features.*
import kotlinx.coroutines.launch

@Composable
fun AuthenticatedNavHost(
    outerNavController: NavHostController,
    role: String,
    email: String
) {
    val innerNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = innerNavController,
                drawerState = drawerState,
                scope = scope,
                onLogout = {
                    // Navigate back to login, clearing the back stack
                    outerNavController.navigate("login") {
                        popUpTo(outerNavController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            content = { innerPadding ->
                NavHost(
                    navController = innerNavController,
                    startDestination = "home",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("home") {
                        HomeScreenWithDrawer(role, innerNavController) {
                            outerNavController.navigate("login") {
                                popUpTo(outerNavController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    }
                    composable("announcements") {
                        when (role.lowercase()) {
                            "alumni" -> AlumniAnnouncementsScreen(email = email, navController = innerNavController)
                            "collegeadmin" -> CollegeAdminAnnouncementsScreen(email = email, navController = innerNavController)
                            else -> StudentAnnouncementsScreen(email = email, navController = innerNavController)
                        }
                    }
                    composable("company_insights") {
                        when (role.lowercase()) {
                            "alumni" -> AlumniCompanyInsightsScreen(email = email, navController = innerNavController)
                            "collegeadmin" -> CollegeAdminCompanyInsightsScreen(email = email, navController = innerNavController)
                            else -> StudentCompanyInsightsScreen(email = email, navController = innerNavController)
                        }
                    }
                    composable("jobs") {
                        when (role.lowercase()) {
                            "alumni" -> AlumniJobPortalScreen(email = email, navController = innerNavController)
                            "collegeadmin" -> CollegeAdminJobPortalScreen(email = email, navController = innerNavController)
                            else -> StudentJobPortalScreen(email = email, navController = innerNavController)
                        }
                    }
                    composable("events") {
                        when (role.lowercase()) {
                            "alumni" -> AlumniEventsScreen(email = email, navController = innerNavController)
                            "collegeadmin" -> CollegeAdminEventsScreen(email = email, navController = innerNavController)
                            else -> StudentEventsScreen(email = email, navController = innerNavController)
                        }
                    }
                    composable("mentorship") {
                        when (role.lowercase()) {
                            "alumni" -> AlumniMentorshipScreen(email = email, navController = innerNavController)
                            "collegeadmin" -> CollegeAdminMentorshipScreen(email = email, navController = innerNavController)
                            else -> StudentMentorshipScreen(email = email, navController = innerNavController)
                        }
                    }
                    composable("success_stories") {
                        when (role.lowercase()) {
                            "alumni" -> AlumniSuccessStoriesScreen(email = email, navController = innerNavController)
                            "collegeadmin" -> CollegeAdminSuccessStoriesScreen(email = email, navController = innerNavController)
                            else -> StudentSuccessStoriesScreen(email = email, navController = innerNavController)
                        }
                    }
                    composable("discussion/{threadLink}") { backStackEntry ->
                        val encodedLink = backStackEntry.arguments?.getString("threadLink") ?: ""
                        val realLink = Uri.decode(encodedLink)

                        // Now you have the full "http://129.154.249.30:8080/d/7-eeeeee" URL in realLink
                        DiscussionScreen(navController = innerNavController, threadLink = realLink)
                    }
                    composable("jobApplications/{jobTitle}") { backStackEntry ->
                        val jobTitle = backStackEntry.arguments?.getString("jobTitle") ?: ""
                        AlumniJobApplicationsScreen(encodedJobTitle = jobTitle, navController = innerNavController)
                    }

                }
            }
        )
    }
}