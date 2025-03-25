package com.example.dbit_almaconnect3.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

// Request/Response data classes

data class JobPostingRequest(
    val title: String,
    val description: String,
    val postedBy: String, // Email field in PocketBase
    val postedAt: String,  // Datetime in ISO format
    val discussionLink: String? = null // store Flarum discussion URL
)

data class JobPostingResponse(
    val id: String,
    val title: String,
    val description: String,
    val postedBy: String,
    val postedAt: String,
    val discussionLink: String? // URL from Flarum
)

// Wrapper class for the jobs response
data class JobListWrapper(
    val items: List<JobPostingResponse>
)

// Application endpoints
data class ApplicationRequest(
    val job: String,         // Job ID (relation)
    val appliedBy: String,   // Changed field name to match PocketBase
    val resume: String
)

data class ApplicationResponse(
    val id: String,
    val job: String?,          // Mark as nullable if it can be null
    val appliedBy: String?,
    val appliedAt: String?,
    val status: String?,
    val feedback: String?,
    val resume: List<String>?
)



// New wrapper class for applications response
data class ApplicationListWrapper(
    val items: List<ApplicationResponse>
)

interface JobPortalService {

    // Jobs endpoints
    @GET("api/collections/jobs/records")
    suspend fun getJobs(): Response<JobListWrapper>

    @GET("api/collections/jobs/records/{id}")
    suspend fun getJob(@Path("id") id: String): Response<JobPostingResponse>

    @POST("api/collections/jobs/records")
    suspend fun postJob(@Body request: JobPostingRequest): Response<JobPostingResponse>

    @PATCH("api/collections/jobs/records/{id}")
    suspend fun updateJob(@Path("id") id: String, @Body request: Map<String, Any>): Response<JobPostingResponse>

    @DELETE("api/collections/jobs/records/{id}")
    suspend fun deleteJob(@Path("id") id: String): Response<Unit>

    // Applications endpoints
    @GET("api/collections/applications/records")
    suspend fun getApplications(): Response<List<ApplicationResponse>>

    // New: Get applications filtered by job ID. Adjust the filter query parameter according to your PocketBase syntax.
    @GET("api/collections/applications/records")
    suspend fun getApplicationsForJob(@Query("filter") filter: String): Response<ApplicationListWrapper>

    @GET("api/collections/applications/records/{id}")
    suspend fun getApplication(@Path("id") id: String): Response<ApplicationResponse>

    // Updated: Multipart endpoint for file upload along with additional fields
    @Multipart
    @POST("api/collections/applications/records")
    suspend fun applyForJobMultipart(
        @Part("job") job: RequestBody,
        @Part("appliedBy") appliedBy: RequestBody,  // Changed key from "applicant" to "appliedBy"
        @Part("appliedAt") appliedAt: RequestBody,
        @Part("status") status: RequestBody,
        @Part resume: MultipartBody.Part
    ): Response<ApplicationResponse>

    @PATCH("api/collections/applications/records/{id}")
    suspend fun updateApplication(@Path("id") id: String, @Body request: Map<String, Any>): Response<ApplicationResponse>

    @DELETE("api/collections/applications/records/{id}")
    suspend fun deleteApplication(@Path("id") id: String): Response<Unit>
}
