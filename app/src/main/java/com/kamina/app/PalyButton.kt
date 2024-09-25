package com.kamina.app

import android.content.Intent
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.kamina.app.api.fetchUserProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier


@Composable
fun PlayButton(
    entityId: Int,
    isMovie: Int,
    videoFilePath: String?,
    userId: Int,
    modifier: Modifier = Modifier // Add this parameter
) {
    val context = LocalContext.current
    var buttonText by remember { mutableStateOf("Play Series") }
    var nextSeason by remember { mutableStateOf(1) }
    var nextEpisode by remember { mutableStateOf(1) }

    // Fetch user progress for series
    LaunchedEffect(entityId, userId) {
        CoroutineScope(Dispatchers.IO).launch {
            val userProgress = fetchUserProgress(userId, entityId)
            if (userProgress != null) {
                if (userProgress.isWatched) {
                    buttonText = "Continue"
                    nextSeason = userProgress.currentSeason
                    nextEpisode = userProgress.currentEpisode + 1
                } else {
                    nextSeason = userProgress.currentSeason
                    nextEpisode = userProgress.currentEpisode
                }
                Log.d("PlayButton", "User progress found: season $nextSeason, episode $nextEpisode")
            } else {
                Log.d("PlayButton", "No user progress found, starting from S1E1")
            }
        }
    }

    // Apply the passed modifier to the Button
    Button(
        onClick = {
            Log.d("PlayButton", "Button clicked. userId: $userId, entityId: $entityId, isMovie: $isMovie")

            if (isMovie == 1 && videoFilePath != null) {
                val intent = Intent(context, WatchPageActivity::class.java).apply {
                    putExtra("videoUrl", videoFilePath)
                    putExtra("userId", userId)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(context, WatchPage::class.java).apply {
                    putExtra("entityId", entityId)
                    putExtra("season", nextSeason)
                    putExtra("episode", nextEpisode)
                    putExtra("userId", userId)
                }
                context.startActivity(intent)
            }
        },
        modifier = modifier // Apply the modifier here
    ) {
        Text(text = if (isMovie == 1) "Play Movie" else buttonText)
    }
}


