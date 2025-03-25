package com.example.dbit_almaconnect3.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.dbit_almaconnect3.data.api.JobPortalService

object RetrofitInstance {
    private const val BASE_URL = "https://your-pocketbase-url.com/"

    val api: JobPortalService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JobPortalService::class.java)
    }
}
