package com.example.dbit_almaconnect3.data.repository

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthRepository {

    private val pocketBaseUrl = "http://129.154.249.30:8091"
    private val flarumUrl = "http://129.154.249.30:8080"
    private val flarumApiKey = "9bf5f86b94d5873bf57808689182723dc93c6fdc"
    private val client = OkHttpClient()

    // Sign-up for both PocketBase and Flarum
    fun signupUser(
        email: String,
        password: String,
        role: String,
        onSuccess: (String, String) -> Unit, // (email, role)
        onError: (String) -> Unit
    ) {
        val pbUrl = "$pocketBaseUrl/api/collections/users/records"
        // Use the base username for both student and alumni.
        val baseUsername = email.split("@")[0].replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val username = baseUsername

        val pbJson = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("passwordConfirm", password)
            put("username", username)
            put("role", role)
        }
        // Replaced RequestBody.create(...) with toRequestBody(...)
        val pbBody = pbJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val pbRequest = Request.Builder()
            .url(pbUrl)
            .post(pbBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(pbRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onError("PocketBase Signup Failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBodyString = response.body?.string()
                if (response.isSuccessful) {
                    // Pass the same username to Flarum.
                    signupFlarumUser(email, password, role, username, onSuccess, onError)
                } else {
                    onError("PocketBase Signup failed: $responseBodyString")
                }
            }
        })
    }

    // Flarum sign-up with username passed in.
    private fun signupFlarumUser(
        email: String,
        password: String,
        role: String,
        username: String,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val flUrl = "$flarumUrl/api/users"
        val flJson = JSONObject().apply {
            put("data", JSONObject().apply {
                put("type", "users")
                put("attributes", JSONObject().apply {
                    put("username", username) // Use the same username generated earlier.
                    put("email", email)
                    put("password", password)
                    put("isEmailConfirmed", true)
                })
            })
        }
        // Replaced RequestBody.create(...) with toRequestBody(...)
        val flBody = flJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val flRequest = Request.Builder()
            .url(flUrl)
            .post(flBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Token $flarumApiKey")
            .build()

        client.newCall(flRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onError("Flarum Signup Failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess(email, role)
                } else {
                    val errorBody = response.body?.string()
                    Log.e("FlarumSignup", "Signup error: $errorBody")
                    onError("Flarum Signup failed: $errorBody")
                }
            }
        })
    }

    // Login to PocketBase and then to Flarum
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String, String) -> Unit, // (email, role)
        onError: (String) -> Unit
    ) {
        val pbUrl = "$pocketBaseUrl/api/collections/users/auth-with-password"
        val pbJson = JSONObject().apply {
            put("identity", email)
            put("password", password)
        }
        // Replaced RequestBody.create(...) with toRequestBody(...)
        val pbBody = pbJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val pbRequest = Request.Builder()
            .url(pbUrl)
            .post(pbBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(pbRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onError("PocketBase Login Failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBodyString = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBodyString ?: "")
                    val token = jsonResponse.optString("token")
                    // Extract role from the PocketBase record.
                    val role = jsonResponse.getJSONObject("record").optString("role")

                    if (token.isNotEmpty()) {
                        // Pass the role from PocketBase into loginFlarum
                        loginFlarum(email, password, role, onSuccess, onError)
                    } else {
                        onError("Login failed: Missing token")
                    }
                } catch (e: Exception) {
                    onError("Login Parsing Failed: ${e.message}")
                }
            }
        })
    }

    // Updated Flarum Login function to accept the role from PocketBase.
    private fun loginFlarum(
        email: String,
        password: String,
        role: String,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val flUrl = "$flarumUrl/api/token"
        val flJson = JSONObject().apply {
            put("identification", email)
            put("password", password)
            put("remember", 1)
        }
        // Replaced RequestBody.create(...) with toRequestBody(...)
        val flBody = flJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val flRequest = Request.Builder()
            .url(flUrl)
            .post(flBody)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build()

        client.newCall(flRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onError("Flarum Login Failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    // Pass along the actual role retrieved from PocketBase.
                    onSuccess(email, role)
                } else {
                    onError("Flarum Login Failed: ${response.body?.string()}")
                }
            }
        })
    }

    // Legacy Flarum Login (if needed for backward compatibility)
    private fun loginFlarum(
        email: String,
        password: String,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val flUrl = "$flarumUrl/api/token"
        val flJson = JSONObject().apply {
            put("identification", email)
            put("password", password)
            put("remember", 1)
        }
        // Replaced RequestBody.create(...) with toRequestBody(...)
        val flBody = flJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val flRequest = Request.Builder()
            .url(flUrl)
            .post(flBody)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build()

        client.newCall(flRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onError("Flarum Login Failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess(email, "Authenticated")
                } else {
                    onError("Flarum Login Failed: ${response.body?.string()}")
                }
            }
        })
    }

    // Logout from both PocketBase and Flarum
    fun logoutUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // PocketBase does not require an explicit logout for token-based systems.
            val flUrl = "$flarumUrl/api/token"
            val flRequest = Request.Builder()
                .url(flUrl)
                .delete()
                .header("Authorization", "Token $flarumApiKey")
                .build()

            client.newCall(flRequest).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    onError("Logout Failed: ${e.message}")
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("Flarum Logout Failed: ${response.body?.string()}")
                    }
                }
            })
        } catch (e: Exception) {
            onError("Logout Exception: ${e.message}")
        }
    }
}
