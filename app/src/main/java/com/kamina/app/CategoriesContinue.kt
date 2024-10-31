package com.kamina.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.ThumbnailData
import com.kamina.app.api.fetchContinueWatching

@Composable
fun CategoriesContinue(userId: String, userLanguage: String) {  // Ensure userLanguage is passed here
    var thumbnails by remember { mutableStateOf<List<ThumbnailData>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // API call to fetch "Continue Watching" content with userLanguage
    LaunchedEffect(userId, userLanguage) {
        fetchContinueWatching(userId, userLanguage) { result ->  // Pass userLanguage here
            thumbnails = result?.filter { !it.name.isNullOrEmpty() && !it.thumbnail.isNullOrEmpty() }
            isLoading = false
        }
    }

    if (!thumbnails.isNullOrEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = "Continue Watching",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(thumbnails!!) { thumbnail ->
                    ThumbnailItem(thumbnail)
                }
            }
        }
    } else if (isLoading) {
        Text(
            text = "Loading...",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
fun ThumbnailItem(thumbnail: ThumbnailData) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(thumbnail.thumbnail),
            contentDescription = thumbnail.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
    }
}
