package com.kamina.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.kamina.app.ui.theme.KaminaAppTheme

class WatchPageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""

        setContent {
            KaminaAppTheme {
                WatchPageScreen(videoUrl)
            }
        }
    }
}

@Composable
fun WatchPageScreen(videoUrl: String) {
    WebViewContainer(videoUrl)
}

@Suppress("RedundantOverride")
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(videoUrl: String) {
    val webView = WebView(LocalContext.current).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW  // Allow mixed content
        setLayerType(View.LAYER_TYPE_HARDWARE, null)  // Hardware acceleration

        // Clear cache and history to avoid stale content
        clearCache(true)
        clearHistory()

        // Check for dark mode support and disable it if supported
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
        }
    }

    webView.webViewClient = object : WebViewClient() {
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            // Handle errors if needed
        }
    }

    webView.webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.grant(request.resources)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    ) {
        it.loadUrl(videoUrl)
    }
}
