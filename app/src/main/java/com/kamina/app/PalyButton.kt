package com.kamina.app

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kamina.app.api.fetchEpisodesInSeason
import com.kamina.app.api.fetchUserEntityStatusSuspend
import com.kamina.app.api.fetchUserProgress
import com.kamina.app.ui.components.CustomButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlayButton(
    entityId: Int,
    isMovie: Int,
    videoFilePath: String?,
    userId: Int,
    userLanguage: String,  // Pass the userLanguage parameter
    modifier: Modifier = Modifier // Allow for passing a modifier
) {
    val context = LocalContext.current
    var buttonText by remember { mutableStateOf(if (isMovie == 1) "Play Movie" else "Play Series") }
    var nextSeason by remember { mutableStateOf(1) }
    var nextEpisode by remember { mutableStateOf(1) }

    // Check user entity status and progress
    LaunchedEffect(entityId, userId) {
        CoroutineScope(Dispatchers.IO).launch {
            // Fetch entity status directly with the suspend function
            val status = fetchUserEntityStatusSuspend(userId, entityId)
            if (status != null && status.status == 2) {  // Status code 2 means "Completed"
                buttonText = "Watch Again"
            } else {
                // Fetch user progress for series
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

                    // Fetch total episodes in the current season to handle end of season
                    val totalEpisodes = fetchEpisodesInSeason(entityId, nextSeason, userLanguage)
                    if (nextEpisode > totalEpisodes) {
                        nextSeason++
                        nextEpisode = 1
                    }
                    Log.d("PlayButton", "User progress found: season $nextSeason, episode $nextEpisode")
                } else {
                    Log.d("PlayButton", "No user progress found, starting from S1E1")
                }
            }
        }
    }

    // Apply the passed modifier to the Button
    CustomButton(
        text = buttonText,
        onClick = {
            Log.d("PlayButton", "Button clicked. userId: $userId, entityId: $entityId, isMovie: $isMovie")

            if (isMovie == 1 && videoFilePath != null) {
                // Movie play logic
                val intent = Intent(context, WatchPageActivity::class.java).apply {
                    putExtra("videoUrl", videoFilePath)
                    putExtra("userId", userId)
                    putExtra("userLanguage", userLanguage)
                }
                context.startActivity(intent)
            } else {
                // Series play logic
                val intent = Intent(context, WatchPage::class.java).apply {
                    putExtra("entityId", entityId)
                    putExtra("season", nextSeason)
                    putExtra("episode", nextEpisode)
                    putExtra("userId", userId)
                    putExtra("userLanguage", userLanguage)
                }
                context.startActivity(intent)
            }
        },
        modifier = modifier
    )
}
