package com.example.dbit_almaconnect3.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class Tag(
    val id: String,
    val name: String,
    val slug: String,
    val color: String?,
    val parentId: String? = null  // Optional parent relationship
)

class FlarumTagRepository {
    private val flarumUrl = "http://129.154.249.30:8080"
    private val flarumApiKey = "9bf5f86b94d5873bf57808689182723dc93c6fdc"
    private val client = OkHttpClient()

    // Fetch tags, including parent relationship if available.
    suspend fun getTags(): List<Tag>? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$flarumUrl/api/tags?page[limit]=100")
            .header("Authorization", "Token $flarumApiKey")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: return@withContext null
            val jsonObj = JSONObject(responseBody)
            val dataArray = jsonObj.getJSONArray("data")
            val tags = mutableListOf<Tag>()
            for (i in 0 until dataArray.length()) {
                val tagObj = dataArray.getJSONObject(i)
                val id = tagObj.getString("id")
                val attributes = tagObj.getJSONObject("attributes")
                val name = attributes.getString("name")
                val slug = attributes.optString("slug", "")
                val color = attributes.optString("color", null)
                val parentId = tagObj.optJSONObject("relationships")
                    ?.optJSONObject("parent")
                    ?.optJSONObject("data")
                    ?.optString("id")
                tags.add(Tag(id, name, slug, color, parentId))
            }
            tags
        } else {
            Log.e("FlarumTagRepository", "Failed to get tags: ${response.code}")
            null
        }
    }

    // Get primary mentorship tag ID by slug.
    suspend fun getPrimaryMentorshipTagId(): String? = withContext(Dispatchers.IO) {
        getTags()?.firstOrNull { it.slug == "community-based-mentorship" }?.id
    }

    // Create tag with optional parentTagId.
    suspend fun createTag(
        name: String,
        color: String? = "#ffffff",
        parentTagId: String? = null
    ): Tag? = withContext(Dispatchers.IO) {
        val finalColor = color?.trim()?.lowercase() ?: "#ffffff"
        val slug = name.lowercase().trim().replace("\\s+".toRegex(), "-")
        val payload = JSONObject().apply {
            put("data", JSONObject().apply {
                put("type", "tags")
                put("attributes", JSONObject().apply {
                    put("name", name)
                    put("slug", slug)
                    put("description", "")
                    put("color", finalColor)
                })
                parentTagId?.let {
                    put("relationships", JSONObject().apply {
                        put("parent", JSONObject().apply {
                            put("data", JSONObject().apply {
                                put("type", "tags")
                                put("id", it)
                            })
                        })
                    })
                }
            })
        }

        val body = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$flarumUrl/api/tags")
            .post(body)
            .header("Content-Type", "application/json")
            .header("Authorization", "Token $flarumApiKey")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: return@withContext null
            val dataObj = JSONObject(responseBody).getJSONObject("data")
            val id = dataObj.getString("id")
            val attributes = dataObj.getJSONObject("attributes")
            val tagName = attributes.getString("name")
            val tagSlug = attributes.optString("slug", slug)
            val tagColor = attributes.optString("color", finalColor)
            Tag(id, tagName, tagSlug, tagColor, parentTagId)
        } else {
            Log.e("FlarumTagRepository", "Failed to create tag: ${response.code}")
            
            // Special handling for 422 errors - these sometimes create the tag anyway
            if (response.code == 422) {
                Log.w("FlarumTagRepository", "Received 422 error but checking if tag was created anyway")
                
                // Give the server a moment to process
                kotlinx.coroutines.delay(1000)
                
                // Check if the tag was created despite the error
                val allTags = getTags()
                val createdTag = allTags?.firstOrNull { it.slug == slug || it.name == name }
                
                if (createdTag != null) {
                    Log.i("FlarumTagRepository", "Tag was created despite 422 error: ${createdTag.name}")
                    return@withContext createdTag
                }
            }
            
            null
        }
    }
}
