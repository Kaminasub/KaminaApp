package com.kamina.app

import android.os.Bundle
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

class WatchPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Embed video URL
        val testUrl = "https://ok.ru/videoembed/7488021465732?autoplay=1&rel=0&Showinfo=0&title=false"

        setContent {
            WatchPageScreen(videoUrl = testUrl)
        }
    }
}

@Composable
fun WatchPageScreen(videoUrl: String) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                // Enable JavaScript
                settings.javaScriptEnabled = true

                // Enable DOM storage for video players
                settings.domStorageEnabled = true

                // Allow mixed content (for HTTP/HTTPS mismatch)
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // Enable video playback without requiring user interaction
                settings.mediaPlaybackRequiresUserGesture = false

                // Set WebViewClient to handle loading events
                webViewClient = WebViewClient()

                // Inject JavaScript to handle the embedded video
                addJavascriptInterface(JavaScriptInterface(this), "Android")

                // Load the video URL
                loadUrl(videoUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

class JavaScriptInterface(private val webView: WebView) {

    @android.webkit.JavascriptInterface
    fun playVideo() {
        webView.evaluateJavascript(
            """
            (function() {
                var video = document.querySelector('video');
                if (video) {
                    video.play();
                }
            })();
            """, null
        )
    }
}
