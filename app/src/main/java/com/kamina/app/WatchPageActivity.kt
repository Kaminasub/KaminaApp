package com.kamina.app

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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

@Composable
fun WebViewContainer(videoUrl: String) {
    val webView = WebView(LocalContext.current).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        loadUrl(videoUrl)
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}
