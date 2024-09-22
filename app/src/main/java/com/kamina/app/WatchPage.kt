package com.kamina.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamina.app.api.fetchWatchPageEpisode
import com.kamina.app.api.fetchWatchPageUserProgress
import com.kamina.app.api.fetchNextEpisode
import com.kamina.app.api.WatchPageEpisode
import com.kamina.app.api.WatchPageUserProgress
import kotlinx.coroutines.launch

@Composable
fun WatchPage(entityId: Int, season: Int, episode: Int, userId: Int) {
    var currentEpisode by remember { mutableStateOf<WatchPageEpisode?>(null) }
    var nextEpisode by remember { mutableStateOf<WatchPageEpisode?>(null) }
    var userProgress by remember { mutableStateOf<WatchPageUserProgress?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(entityId, season, episode) {
        coroutineScope.launch {
            // Fetch the current episode
            currentEpisode = fetchWatchPageEpisode(entityId, season, episode)
            currentEpisode?.let { episodeData ->
                // Fetch user progress for the current episode
                userProgress = fetchWatchPageUserProgress(userId, entityId, episodeData.id)

                // Fetch the next episode
                nextEpisode = fetchNextEpisode(entityId, episodeData.season, episodeData.episode)
            }
        }
    }

    currentEpisode?.let { episodeData ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "S${episodeData.season} E${episodeData.episode}: ${episodeData.title}")
            Spacer(modifier = Modifier.height(16.dp))

            // VideoPlayer will handle video playing
            VideoPlayer(filePath = episodeData.filePath)

            Spacer(modifier = Modifier.height(16.dp))

            nextEpisode?.let {
                Button(onClick = { handleContinue(episodeData, nextEpisode) }) {
                    Text(text = "Continue to next episode")
                }
            }
        }
    } ?: Text(text = "Loading episode...")
}

// Simplified video player for demonstration
@Composable
fun VideoPlayer(filePath: String) {
    Text(text = "Playing video from $filePath")
}

// Simplified continue handling for demonstration
fun handleContinue(currentEpisode: WatchPageEpisode, nextEpisode: WatchPageEpisode?) {
    // Handle marking the episode as watched and transitioning to the next episode
}
