package com.example.dbit_almaconnect3.ui.screens.features

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dbit_almaconnect3.data.api.ApplicationResponse
import com.example.dbit_almaconnect3.viewmodel.JobPortalViewModel
import androidx.core.net.toUri

@Composable
fun AlumniJobApplicationsScreen(jobTitle: String, navController: NavController) {
    val viewModel: JobPortalViewModel = viewModel()
    val applications by viewModel.applications.collectAsState()

    // Fetch applications for the given job title
    LaunchedEffect(jobTitle) {
        viewModel.fetchApplicationsForJob(jobTitle)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Applications for Job: $jobTitle", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(applications) { app ->
                ApplicationCard(app)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Job Postings")
        }
    }
}




@Composable
fun ApplicationCard(application: ApplicationResponse) {
    val context = LocalContext.current
    val viewModel: JobPortalViewModel = viewModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Applied By: ${application.appliedBy}", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Applied At: ${application.appliedAt}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "View Resume",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    // Retrieve the complete resume URL from the list
                    val resumeUrl = application.resume?.firstOrNull()
                    if (resumeUrl.isNullOrBlank()) {
                        Toast.makeText(context, "No resume available", Toast.LENGTH_SHORT).show()
                    } else {
                        // Here, we assume that resumeUrl is a complete URL.
                        val resumeUri = resumeUrl.toUri()
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(resumeUri, "application/pdf")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "No app available to view the resume", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Delete Application button
            Button(
                onClick = {
                    viewModel.deleteApplication(application.id) {
                        Toast.makeText(context, "Application deleted", Toast.LENGTH_SHORT).show()
                        // Optionally, refresh the application list here
                    }
                }
            ) {
                Text("Delete Application")
            }
        }
    }
}
