package com.kamina.app.ui

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kamina.app.ui.theme.KaminaAppTheme

class EmbeddedVideoPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hlsUrl = "https://ok.ru/videoembed/7488021465732?autoplay=1&rel=0&Showinfo=0&title=false"

        setContent {
            KaminaAppTheme {
                // Create a WebView to load the embedded video
                WebView(this).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(hlsUrl)
                }
            }
        }
    }
}
