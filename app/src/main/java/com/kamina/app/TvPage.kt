package com.kamina.app

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.Channel
import com.kamina.app.api.TvPageApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun TvPage(userId: Int) {
    val channels = remember { mutableStateOf<List<Channel>?>(null) }
    val selectedChannelUrl = remember { mutableStateOf<String?>(null) }

    // Fetch the list of channels from the API
    LaunchedEffect(Unit) {
        try {
            val response = TvPageApiClient.api.getChannels()
            if (response.isSuccessful) {
                channels.value = response.body()
            } else {
                Log.e("TvPage", "Failed to fetch channels: ${response.errorBody()?.string()}")
                channels.value = emptyList() // Handle error by showing an empty list
            }
        } catch (e: Exception) {
            Log.e("TvPage", "Error fetching channels", e)
            channels.value = emptyList()
        }
    }

    if (channels.value == null) {
        // Show loading indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading channels...", color = Color.White, fontSize = 16.sp)
        }
    } else {
        // Main layout
        Row(modifier = Modifier.fillMaxSize()) {
            // Left column: Channel list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.DarkGray)
            ) {
                items(channels.value ?: emptyList()) { channel ->
                    ChannelListItem(channel = channel) { selectedChannel ->
                        // Trigger a channel selection
                        fetchStreamUrl(selectedChannel.streamUrl) { streamUrl ->
                            selectedChannelUrl.value = streamUrl
                        }
                    }
                }
            }

            // Right column: Video player
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                ChannelPlayer(selectedChannelUrl.value)
            }
        }
    }
}

@Composable
fun ChannelListItem(channel: Channel, onChannelClick: (Channel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Gray)
            .clickable { onChannelClick(channel) }
    ) {
        Image(
            painter = rememberAsyncImagePainter(channel.tvgLogo),
            contentDescription = channel.name,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = channel.name,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

fun fetchStreamUrl(streamUrl: String, onResult: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val streamResponse = TvPageApiClient.api.getStreamUrl(streamUrl)
            if (streamResponse.isSuccessful) {
                // Read the response as plain text
                val streamUrlBody = streamResponse.body()?.string()
                onResult(streamUrlBody)
            } else {
                Log.e("TvPage", "Failed to fetch stream URL: ${streamResponse.errorBody()?.string()}")
                onResult(null) // Handle error gracefully
            }
        } catch (e: Exception) {
            Log.e("TvPage", "Error fetching stream URL", e)
            onResult(null)
        }
    }
}


@Composable
fun ChannelPlayer(streamUrl: String?) {
    val context = LocalContext.current
    val vlcLib = remember { LibVLC(context, arrayListOf("--network-caching=1500", "--avcodec-hw=any")) }
    val mediaPlayer = remember { MediaPlayer(vlcLib) }
    var playbackUrl by remember { mutableStateOf<String?>(null) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(streamUrl) {
        if (streamUrl != null) {
            try {
                // Fetch the actual playable URL
                playbackUrl = fetchPlayableUrl(streamUrl)
                if (playbackUrl != null) {
                    Log.d("ChannelPlayer", "Final playback URL: $playbackUrl")
                    val media = Media(vlcLib, Uri.parse(playbackUrl)).apply {
                        addOption(":network-caching=1500")
                        addOption(":http-user-agent=user-agent") // User-Agent from your API
                        addOption(":http-referrer=https://example.com") // If required
                    }
                    mediaPlayer.media = media
                    mediaPlayer.play()
                } else {
                    playbackError = "Failed to resolve playback URL"
                    Log.e("ChannelPlayer", playbackError!!)
                }
            } catch (e: Exception) {
                playbackError = "Error initializing VLC MediaPlayer: ${e.message}"
                Log.e("ChannelPlayer", playbackError!!, e)
            }
        }

        onDispose {
            Log.d("ChannelPlayer", "Releasing VLC MediaPlayer")
            mediaPlayer.release()
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (playbackUrl != null) {
            AndroidView(
                factory = {
                    org.videolan.libvlc.util.VLCVideoLayout(context).apply {
                        mediaPlayer.attachViews(this, null, false, false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playbackError ?: "Loading...",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

private fun fetchPlayableUrl(streamUrl: String): String? {
    return try {
        if (isM3UPlaylist(streamUrl)) {
            val playlistContent = fetchPlaylistContent(streamUrl)
            extractPlayableUrlFromPlaylist(playlistContent)
        } else {
            streamUrl
        }
    } catch (e: Exception) {
        Log.e("ChannelPlayer", "Error fetching or parsing playlist: ${e.message}", e)
        null
    }
}

private fun isM3UPlaylist(url: String): Boolean {
    return url.endsWith(".m3u") || url.endsWith(".m3u8")
}

private fun fetchPlaylistContent(url: String): String {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connectTimeout = 5000
    connection.readTimeout = 5000
    return connection.inputStream.bufferedReader().use { it.readText() }
}

private fun extractPlayableUrlFromPlaylist(content: String): String? {
    return content.lines().find { it.startsWith("http") }
}
