package com.kamina.app

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog

@Composable
fun FullScreenMoviePopup(filePath: String, onClose: () -> Unit) {
    // Implementation for a popup dialog or a full-screen view
    Dialog(onDismissRequest = onClose) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Placeholder for the actual video player (e.g., ExoPlayer or WebView)
            Text(text = "Playing video from: $filePath")

            // Close button to dismiss the full-screen popup
            Button(onClick = onClose) {
                Text(text = "Close")
            }
        }
    }
}



@Composable
fun FullScreenWebView(filePath: String) {
    // Creating the WebView inside Jetpack Compose
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT

            loadUrl(filePath)
        }
    })
}

