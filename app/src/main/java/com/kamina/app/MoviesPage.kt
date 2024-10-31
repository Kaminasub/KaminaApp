package com.kamina.app

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.MoviesCategoryResponse
import com.kamina.app.api.fetchMoviesByCategory


@Composable
fun MoviesPage(userId: Int, userLanguage: String) {
    var moviesCategories by remember { mutableStateOf<List<MoviesCategoryResponse>?>(null) }
    val context = LocalContext.current

    // Fetch movies categories from the API based on the user's language
    LaunchedEffect(userLanguage) {
        Log.d("MoviesPage", "Fetching movies for userLanguage: $userLanguage and userId: $userId")
        fetchMoviesByCategory(userLanguage) { result ->
            moviesCategories = result
        }
    }

    // Display movies categories once fetched
    moviesCategories?.let { moviesCategoryList ->
        // Filter out categories that have no entities with valid translations
        val filteredCategories = moviesCategoryList.filter { category ->
            category.entities.any { entity ->
                entity.translations?.language == userLanguage
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(filteredCategories) { category ->
                // Display each movies category with a language filter applied
                MoviesCategorySection(category = category, userId = userId, userLanguage = userLanguage)
            }
        }
    } ?: run {
        // Loading state
        Text("Loading movies...", color = Color.White)
    }
}

@Composable
fun MoviesCategorySection(category: MoviesCategoryResponse, userId: Int, userLanguage: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Display the category name
        Text(
            text = category.categoryName,
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Display movies thumbnails in a horizontal scroll
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Filter entities based on the existence of a translation for the current language
            val filteredEntities = category.entities.filter { entity ->
                val translation = entity.translations?.language == userLanguage
                if (translation) {
                    Log.d("MoviesPage", "Translation found for entityId: ${entity.id} in language: $userLanguage")
                    true
                } else {
                    Log.d("MoviesPage", "No translation found for entityId: ${entity.id} in language: $userLanguage")
                    false
                }
            }

            // Display filtered entities
            items(filteredEntities) { entity ->
                // Use the entity's thumbnail by default
                val thumbnailUrl = entity.thumbnail ?: ""

                if (!thumbnailUrl.isNullOrEmpty()) {
                    MoviesThumbnailItem(
                        thumbnailUrl = thumbnailUrl,
                        entityId = entity.id,
                        userId = userId,
                        userLanguage = userLanguage
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun MoviesThumbnailItem(thumbnailUrl: String, entityId: Int, userId: Int, userLanguage: String) {
    val context = LocalContext.current  // Get current context for navigation
    var isFocused by remember { mutableStateOf(false) }

    Image(
        painter = rememberAsyncImagePainter(thumbnailUrl),
        contentDescription = "Movies Thumbnail",
        modifier = Modifier
            .width(188.dp)  // Consistent width
            .height(280.dp) // Consistent height
            .padding(start = 7.dp)  // Reduced margin to match categories
            .clip(RoundedCornerShape(12.dp))  // Clip the image for rounded corners
            .onFocusChanged { isFocused = it.isFocused }  // Update focus state
            .focusable()  // Make it focusable for TV navigation
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier.border(
                        width = 0.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
            .clickable {
                // Navigate to DetailPageActivity when thumbnail is clicked
                Log.d("MoviesPage", "Navigating to DetailPage with entityId: $entityId, userId: $userId, userLanguage: $userLanguage")
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("entityId", entityId)  // Pass the entityId to the DetailPageActivity
                    putExtra("userId", userId)      // Pass the userId to the DetailPageActivity
                    putExtra("userLanguage", userLanguage)  // Pass the userLanguage to DetailPageActivity
                }
                context.startActivity(intent)
            },
        contentScale = ContentScale.Crop  // Crop the image to fit the container without distortion
    )
}
