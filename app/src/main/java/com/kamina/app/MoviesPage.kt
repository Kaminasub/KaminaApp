package com.kamina.app

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.MoviesCategoryResponse
import com.kamina.app.api.fetchMoviesByCategory

@Composable
fun MoviesPage() {
    var moviesCategories by remember { mutableStateOf<List<MoviesCategoryResponse>?>(null) }

    // Fetch movies categories from the API
    LaunchedEffect(Unit) {
        fetchMoviesByCategory { result ->
            moviesCategories = result
        }
    }

    // Display movies categories once fetched
    moviesCategories?.let { moviesCategoryList ->
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(moviesCategoryList) { category ->
                // Display each movies category
                MoviesCategorySection(category = category)
            }
        }
    } ?: run {
        // Loading state
        Text("Loading movies...", color = Color.White)
    }
}

@Composable
fun MoviesCategorySection(category: MoviesCategoryResponse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Display the category name
        Text(
            text = category.categoryName,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 10.dp, top = 10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Display movies thumbnails in a horizontal scroll
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(category.entities) { entity ->
                MoviesThumbnailItem(thumbnailUrl = entity.thumbnail, entityId = entity.id)  // Pass entityId to the item
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun MoviesThumbnailItem(thumbnailUrl: String, entityId: Int) {
    val context = LocalContext.current  // Get current context for navigation

    Image(
        painter = rememberAsyncImagePainter(thumbnailUrl),
        contentDescription = "Movies Thumbnail",
        modifier = Modifier
            .width(188.dp)  // Consistent width
            .height(280.dp) // Consistent height
            .padding(start = 7.dp)  // Reduced margin to match categories
            .clip(RoundedCornerShape(12.dp))  // 12dp radius for carousel items
            .background(Color.White)
            .clickable {
                // Navigate to DetailPageActivity when thumbnail is clicked
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("entityId", entityId)  // Pass the entityId to the DetailPageActivity
                }
                context.startActivity(intent)
            },
        contentScale = ContentScale.Crop  // Crop the image to fit the container without distortion
    )
}
