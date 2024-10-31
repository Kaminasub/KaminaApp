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

    // Fetch user progress for series
    LaunchedEffect(entityId, userId) {
        CoroutineScope(Dispatchers.IO).launch {
            val userProgress = fetchUserProgress(userId, entityId)
            if (userProgress != null) {
                // Update button text and progress based on whether the user has watched the content
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
    CustomButton(
        text = buttonText, // Text dynamically based on progress and isMovie flag
        onClick = {
            Log.d("PlayButton", "Button clicked. userId: $userId, entityId: $entityId, isMovie: $isMovie")

            // Handle movie play logic
            if (isMovie == 1 && videoFilePath != null) {
                val intent = Intent(context, WatchPageActivity::class.java).apply {
                    putExtra("videoUrl", videoFilePath)
                    putExtra("userId", userId)
                    putExtra("userLanguage", userLanguage)  // Pass user language to WatchPageActivity
                }
                context.startActivity(intent)
            }
            // Handle series play logic
            else {
                val intent = Intent(context, WatchPage::class.java).apply {
                    putExtra("entityId", entityId)
                    putExtra("season", nextSeason)
                    putExtra("episode", nextEpisode)
                    putExtra("userId", userId)
                    putExtra("userLanguage", userLanguage)  // Pass user language to WatchPage
                }
                context.startActivity(intent)
            }
        },
        modifier = modifier // Apply any external modifier passed to the Button
    )
}
