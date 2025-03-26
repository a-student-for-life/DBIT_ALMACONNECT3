package com.example.dbit_almaconnect3.data.repository

import android.util.Log
import com.example.dbit_almaconnect3.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.io.File

class JobPortalRepository {

    // PocketBase
    private val baseUrl = "http://129.154.249.30:8091/" // Your PocketBase instance URL
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(JobPortalService::class.java)

    // Flarum
    private val flarumUrl = "http://129.154.249.30:8080"
    private val flarumApiKey = "9bf5f86b94d5873bf57808689182723dc93c6fdc"

    // OkHttp client for Flarum requests
    private val flarumClient = OkHttpClient()

    suspend fun getJobs(): List<JobPostingResponse>? {
        val response = service.getJobs()
        return if (response.isSuccessful) {
            response.body()?.items
        } else {
            Log.e("JobPortalRepository", "getJobs failed: code=${response.code()}, body=${response.errorBody()?.string()}")
            null
        }
    }

    suspend fun postJob(title: String, description: String, postedBy: String): JobPostingResponse? = withContext(Dispatchers.IO) {
        val discussionLink = createFlarumDiscussion(title, description)
        val postedAt = getCurrentTimestamp()
        if (discussionLink == null) {
            Log.e("Flarum", "Discussion creation failed or returned null.")
        }
        val request = JobPostingRequest(
            title = title,
            description = description,
            postedBy = postedBy,
            postedAt = postedAt,
            discussionLink = discussionLink
        )
        val response = service.postJob(request)
        if (response.isSuccessful) response.body() else {
            Log.e("JobPortalRepository", "postJob failed: code=${response.code()}, body=${response.errorBody()?.string()}")
            null
        }
    }

    // Updated: Apply for job using file upload and additional fields (appliedAt, status)
    suspend fun applyForJob(jobId: String, applicant: String, resumeFile: File): ApplicationResponse? = withContext(Dispatchers.IO) {
        // Prepare parts for multipart request
        val jobRequest = jobId.toRequestBody("text/plain".toMediaTypeOrNull())
        val appliedByRequest = applicant.toRequestBody("text/plain".toMediaTypeOrNull()) // changed field name from "applicant" to "appliedBy"
        val appliedAt = getCurrentTimestamp().toRequestBody("text/plain".toMediaTypeOrNull())
        val status = "pending".toRequestBody("text/plain".toMediaTypeOrNull())

        // Prepare the resume file part (assuming a PDF; adjust MIME type if needed)
        val fileRequestBody = resumeFile.asRequestBody("application/pdf".toMediaTypeOrNull())
        val resumePart = MultipartBody.Part.createFormData("resume", resumeFile.name, fileRequestBody)

        val response = service.applyForJobMultipart(jobRequest, appliedByRequest, appliedAt, status, resumePart)
        return@withContext if (response.isSuccessful) {
            val appResponse = response.body()
            appResponse?.let { application ->
                // Check if the resume field (assumed to be a list of strings) contains a complete URL.
                val resumeList = application.resume
                if (resumeList != null && resumeList.isNotEmpty()) {
                    val firstResume = resumeList.first()
                    if (!firstResume.startsWith("http", ignoreCase = true)) {
                        // If it doesn't start with http, reconstruct the full URL.
                        // You'll need to know the folder ID for this file. For example:
                        val collectionId = "pbc_2689671926"
                        val folderId = "YOUR_FOLDER_ID" // Replace with the actual folder ID that was generated for this file.
                        val baseUrl = "http://129.154.249.30:8091"
                        val fullUrl = "$baseUrl/api/files/$collectionId/$folderId/$firstResume"
                        // Return a copy of the application with the resume field replaced by the full URL.
                        return@withContext application.copy(resume = listOf(fullUrl))
                    }
                }
                application
            }
        } else {
            Log.e("JobPortalRepository", "applyForJob failed: code=${response.code()}, body=${response.errorBody()?.string()}")
            null
        }
    }


    // New: Fetch all applications for a specific job
    suspend fun getApplicationsForJob(jobTitle: String): List<ApplicationResponse>? = withContext(Dispatchers.IO) {
        // Build the filter using the nested job title
        val filter = "job.title='$jobTitle'"
        val response = service.getApplicationsForJob(filter)
        return@withContext if (response.isSuccessful) {
            response.body()?.items
        } else {
            Log.e("JobPortalRepository", "getApplicationsForJob failed: code=${response.code()}, body=${response.errorBody()?.string()}")
            null
        }
    }

    suspend fun deleteApplication(applicationId: String): Boolean = withContext(Dispatchers.IO) {
        val response = service.deleteApplication(applicationId)
        response.isSuccessful
    }


    /**
     * Delete both the Flarum discussion (if any) and the job record in PocketBase
     */
    suspend fun deleteJobAndDiscussion(jobId: String, discussionLink: String?): Boolean = withContext(Dispatchers.IO) {
        // 1) If there's a discussionLink, parse the discussion ID and delete from Flarum
        discussionLink?.let { link ->
            val regex = Regex("/d/(\\d+)")
            val match = regex.find(link)
            val discussionId = match?.groupValues?.getOrNull(1)

            if (!discussionId.isNullOrEmpty()) {
                // DELETE from Flarum
                val deleteRequest = Request.Builder()
                    .url("$flarumUrl/api/discussions/$discussionId")
                    .delete()
                    .header("Authorization", "Token $flarumApiKey")
                    .build()

                val deleteResponse = flarumClient.newCall(deleteRequest).execute()
                deleteResponse.use { r ->
                    if (!r.isSuccessful) {
                        Log.e("Flarum", "Failed to delete discussion: code=${r.code}, body=${r.body?.string()}")
                    }
                }
            }
        }

        // 2) Delete the job from PocketBase
        val response = service.deleteJob(jobId)
        response.isSuccessful
    }

    // Helper function to get current datetime in ISO 8601 format
    private fun getCurrentTimestamp(): String {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    private suspend fun createFlarumDiscussion(title: String, content: String): String? = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("data", JSONObject().apply {
                put("type", "discussions")
                put("attributes", JSONObject().apply {
                    put("title", title)
                    put("content", content)
                })
            })
        }

        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$flarumUrl/api/discussions")
            .post(body)
            .header("Content-Type", "application/json")
            .header("Authorization", "Token $flarumApiKey")
            .build()

        val response = flarumClient.newCall(request).execute()
        response.use { r ->
            if (r.isSuccessful) {
                val responseBody = r.body?.string() ?: return@use null
                val dataObj = JSONObject(responseBody).getJSONObject("data")
                val discussionId = dataObj.getString("id")
                val slug = dataObj.getJSONObject("attributes").optString("slug")
                if (slug.isNotEmpty()) "$flarumUrl/d/$discussionId-$slug" else "$flarumUrl/d/$discussionId"
            } else {
                Log.e("Flarum", "Discussion creation failed: code=${r.code}, body=${r.body?.string()}")
                null
            }
        }
    }
}
