package com.example.dbit_almaconnect3.data.api

import com.google.gson.annotations.JsonAdapter
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

// Request and Response Data Classes

data class JobPostingRequest(
    val title: String,
    val description: String,
    val postedBy: String,
    val postedAt: String,
    val discussionLink: String? = null
)

data class JobPostingResponse(
    val id: String,
    val title: String,
    val description: String,
    val postedBy: String,
    val postedAt: String,
    val discussionLink: String?
)

data class JobListWrapper(
    val items: List<JobPostingResponse>
)

data class ApplicationRequest(
    val job: String,
    val appliedBy: String,
    val resume: String
)

data class ApplicationResponse(
    val id: String,
    val job: String?,
    val appliedBy: String?,
    val appliedAt: String?,
    val status: String?,
    val feedback: String?,
    // Annotate resume so that the custom deserializer is used for this field.
    @JsonAdapter(ResumeDeserializer::class)
    val resume: List<String>?
)

data class ApplicationListWrapper(
    val items: List<ApplicationResponse>
)

// Retrofit Service Interface

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

    @GET("api/collections/applications/records")
    suspend fun getApplicationsForJob(@Query("filter") filter: String): Response<ApplicationListWrapper>

    @GET("api/collections/applications/records/{id}")
    suspend fun getApplication(@Path("id") id: String): Response<ApplicationResponse>

    @Multipart
    @POST("api/collections/applications/records")
    suspend fun applyForJobMultipart(
        @Part("job") job: RequestBody,
        @Part("appliedBy") appliedBy: RequestBody,
        @Part("appliedAt") appliedAt: RequestBody,
        @Part("status") status: RequestBody,
        @Part resume: MultipartBody.Part
    ): Response<ApplicationResponse>

    @PATCH("api/collections/applications/records/{id}")
    suspend fun updateApplication(@Path("id") id: String, @Body request: Map<String, Any>): Response<ApplicationResponse>

    @DELETE("api/collections/applications/records/{id}")
    suspend fun deleteApplication(@Path("id") id: String): Response<Unit>
}
