package com.example.dbit_almaconnect3.data.api


import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

// Custom deserializer for List<String> to handle both JSON arrays and string values.
class ResumeDeserializer : JsonDeserializer<List<String>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<String> {
        return when {
            json.isJsonArray -> json.asJsonArray.map { it.asString }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> listOf(json.asString)
            else -> emptyList()
        }
    }
}


object RetrofitClient {
    private const val BASE_URL = "http://129.154.249.30:8091/"

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<List<String>>() {}.type, ResumeDeserializer())
        .create()

    private val okHttpClient = OkHttpClient.Builder().build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
