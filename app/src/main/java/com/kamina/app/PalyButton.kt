package com.kamina.app

import android.content.Intent
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun PlayButton(entityId: Int, isMovie: Int, videoFilePath: String?, userId: Int) {
    val context = LocalContext.current

    Button(onClick = {
        if (videoFilePath != null && isMovie == 1) {
            Log.d("PlayButton", "Starting WatchPageActivity for movie")
            val intent = Intent(context, WatchPageActivity::class.java).apply {
                putExtra("videoUrl", videoFilePath)
            }
            context.startActivity(intent)
        } else {
            Log.d("PlayButton", "Video file path is missing or not for a movie")
        }
    }) {
        Text(text = "Play Movie")
    }
}
