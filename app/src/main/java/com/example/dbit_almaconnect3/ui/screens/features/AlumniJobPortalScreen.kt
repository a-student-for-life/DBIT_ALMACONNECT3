package com.example.dbit_almaconnect3.ui.screens.features

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dbit_almaconnect3.data.api.JobPostingResponse
import com.example.dbit_almaconnect3.viewmodel.JobPortalViewModel

@Composable
fun AlumniJobPortalScreen(email: String, navController: NavController) {
    val viewModel: JobPortalViewModel = viewModel()
    val jobs by viewModel.jobs.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var jobTitle by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchJobs()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Welcome, $email", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { showDialog = true }) {
            Text("Post a New Job/Internship")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Your Posted Jobs:", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(jobs) { job ->
                // Only display jobs posted by this alumni
                if (job.postedBy == email) {
                    JobPostingCardForAlumni(job, navController)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Post a New Job") },
            text = {
                Column {
                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("Job Title") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jobDescription,
                        onValueChange = { jobDescription = it },
                        label = { Text("Job Description") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.postJob(jobTitle, jobDescription, email) {
                        showDialog = false
                        jobTitle = ""
                        jobDescription = ""
                    }
                }) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JobPostingCardForAlumni(job: JobPostingResponse, navController: NavController) {
    // Access the same ViewModel or pass it in
    val viewModel: JobPortalViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray)
            .padding(8.dp)
    ) {
        Text(text = job.title, fontSize = 20.sp)
        Text(text = job.description, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // If there's a discussion link, show a button to open it
        if (!job.discussionLink.isNullOrEmpty()) {
            Button(onClick = {
                // Encode the full URL so it can be passed as a single route argument
                val encodedLink = Uri.encode(job.discussionLink)
                navController.navigate("discussion/$encodedLink")
            }) {
                Text("Open Discussion Forum")
            }
        }
        // Button to view applications (resumes) for this job
        Button(onClick = {
            // Navigate to the applications screen, passing the job title (URL-encoded)
            navController.navigate("jobApplications/${Uri.encode(job.title)}")
        }) {
            Text("View Applications")
        }
        // Delete button
        Button(onClick = {
            viewModel.deleteJob(job.id, job.discussionLink) {
                // Optionally show a toast or do something on success
            }
        }) {
            Text("Delete Job")
        }
    }
}
