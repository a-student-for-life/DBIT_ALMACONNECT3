package com.example.dbit_almaconnect3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dbit_almaconnect3.data.api.ApplicationResponse
import com.example.dbit_almaconnect3.data.api.JobPostingResponse
import com.example.dbit_almaconnect3.data.repository.JobPortalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class JobPortalViewModel : ViewModel() {
    private val repository = JobPortalRepository()

    private val _jobs = MutableStateFlow<List<JobPostingResponse>>(emptyList())
    val jobs: StateFlow<List<JobPostingResponse>> = _jobs

    // New: State for job applications
    private val _applications = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val applications: StateFlow<List<ApplicationResponse>> = _applications

    private val _applicationResponse = MutableStateFlow<ApplicationResponse?>(null)
    val applicationResponse: StateFlow<ApplicationResponse?> = _applicationResponse

    fun fetchJobs() {
        viewModelScope.launch {
            val jobList = repository.getJobs()
            _jobs.value = jobList ?: emptyList()
        }
    }

    fun postJob(title: String, description: String, postedBy: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.postJob(title, description, postedBy)
            if (result != null) {
                fetchJobs() // Refresh job list after posting
                onSuccess()
            }
        }
    }

    // Updated: applyForJob now uses the resume file and passes applicant info correctly
    fun applyForJob(jobId: String, applicant: String, resumeFile: File, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.applyForJob(jobId, applicant, resumeFile)
            if (result != null) {
                _applicationResponse.value = result
                onSuccess()
            }
        }
    }

    // New: Fetch all applications for a given job posting
    fun fetchApplicationsForJob(jobTitle: String) {
        viewModelScope.launch {
            val apps = repository.getApplicationsForJob(jobTitle)
            _applications.value = apps ?: emptyList()
        }
    }


    /**
     * Delete both the job record and Flarum discussion.
     */
    fun deleteJob(jobId: String, discussionLink: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val isDeleted = repository.deleteJobAndDiscussion(jobId, discussionLink)
            if (isDeleted) {
                fetchJobs() // Refresh after deleting
                onSuccess()
            }
        }
    }

    fun deleteApplication(applicationId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteApplication(applicationId)
            if (success) {
                onSuccess()
                // Optionally refresh the applications list
                // fetchApplicationsForJob(<jobTitle or other filter>)
            } else {
                // Handle error (e.g., show a toast)
            }
        }
    }

}
