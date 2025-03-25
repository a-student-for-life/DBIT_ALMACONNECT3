package com.example.dbit_almaconnect3.ui.screens

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

// Helper function to retrieve auth cookies for the given URL
fun getAuthCookies(url: String): String? {
    val cookieManager = CookieManager.getInstance()
    return cookieManager.getCookie(url)
}

@Composable
fun DiscussionScreen(navController: NavController, threadLink: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                // Enable JavaScript and DOM storage
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.81 Mobile Safari/537.36"

                webViewClient = object : WebViewClient() {
                    // You can override methods for additional handling if needed
                }

                // Set up CookieManager to accept cookies
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)

                // Retrieve authentication cookies (from your main app) for the Flarum domain
                val authCookies = getAuthCookies(threadLink)
                if (!authCookies.isNullOrEmpty()) {
                    // Split and set each cookie for the URL before loading
                    authCookies.split(";").forEach { cookie ->
                        cookieManager.setCookie(threadLink, cookie.trim())
                    }
                }
                // Load the Flarum discussion URL
                loadUrl(threadLink)
            }
        }
    )
}

