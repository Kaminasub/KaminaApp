package com.kamina.app

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import io.github.edsuns.adfilter.AdFilter

class WatchPageActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var adFilter: AdFilter

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AdFilter
        adFilter = AdFilter.get()

        // Set activity to full-screen landscape mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Initialize WebView
        webView = WebView(this)
        setContentView(webView)

        // Configure WebView settings
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        // Set WebView client with ad filtering
        webView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                // Block specific ad-related URLs
                return if (shouldBlockUrl(request.url.toString())) {
                    WebResourceResponse("text/plain", "utf-8", null)
                } else {
                    adFilter.shouldIntercept(view, request)?.resourceResponse
                }
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                adFilter.performScript(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return if (url.startsWith("http://") || url.startsWith("https://")) {
                    false // Allow HTTP and HTTPS URLs
                } else {
                    true // Block other URL schemes
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (error.errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME ||
                    error.errorCode == WebViewClient.ERROR_UNKNOWN) {
                    // Ignore unsupported scheme and unknown errors
                    return
                }
                super.onReceivedError(view, request, error)
            }
        }

        // Enable full-screen video playback
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                setContentView(view)
            }

            override fun onHideCustomView() {
                setContentView(webView)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    if (it.message().contains("ReferenceError")) {
                        Log.d("AdBlocker", "Suppressed console error: ${it.message()} at ${it.sourceId()}:${it.lineNumber()}")
                        return true // Suppress the message
                    }
                }
                return super.onConsoleMessage(consoleMessage) // Default behavior for other messages
            }
        }

        // Load the video URL passed from the previous activity
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        webView.loadUrl(videoUrl)
    }

    // Helper function to match ad URLs
    private fun shouldBlockUrl(url: String): Boolean {
        return url.contains("mc.yandex.ru") ||
                url.contains("googletagmanager.com") ||
                //STREAMWISH///
                url.contains("dalysv.com") ||
                url.contains("media.dalysv.com") ||
                url.contains("media.dalysv.com") ||
                url.contains("jouwaikekaivep.net") ||
                url.contains("outwingullom.com") ||
                url.contains("dnsads.js") ||
                url.contains("code.min.js") ||
                url.contains("roseimgs.com") ||
                url.contains("streamwish.com/js") ||
                url.contains("naupsakiwhy.com") ||
                url.contains("psoroumukr.com") ||
                url.contains("ap.taichnewcal.com") ||


                //OKRU////
                url.contains("ok.ru/dk?cmd=videostatnew") ||
                url.contains("tns-counter.ru") ||
                url.contains("ad.mail.ru") ||
                url.contains("vk.com/js/lang-pack.js") ||
                url.contains("top-fwz1.mail.ru") ||
                url.contains("st.okcdn.ru/static/one-video-player")

    }

    // Handle back press to navigate back within WebView
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

//MOVIES