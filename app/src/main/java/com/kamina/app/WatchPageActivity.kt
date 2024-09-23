package com.kamina.app

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class WatchPageActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the activity to full-screen landscape mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Enable full-screen immersive mode
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Create a WebView
        webView = WebView(this)
        setContentView(webView)

        // Configure WebView settings
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        // Enable full-screen video
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                // Enter full-screen mode when playing video
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                setContentView(view)
            }

            override fun onHideCustomView() {
                // Exit full-screen mode when video is done
                setContentView(webView)
            }
        }

        // Handle redirects inside the WebView
        webView.webViewClient = WebViewClient()

        // Load the video URL passed from the previous activity
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        webView.loadUrl(videoUrl)
    }

    // Handle back press to ensure WebView can navigate back
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
