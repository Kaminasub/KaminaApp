package com.kamina.app

import androidx.compose.foundation.Image
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
import com.kamina.app.api.HomeCategoryResponse
import com.kamina.app.api.fetchHomeCategories

@Composable
fun CategoriesSection(userLanguage: String) {
    var categories by remember { mutableStateOf<List<HomeCategoryResponse>?>(null) }

    // Fetch categories from the API with the language
    LaunchedEffect(userLanguage) {
        fetchHomeCategories(language = userLanguage) { result ->
            categories = result?.filter { category ->
                // Only include categories with entities that have a valid translation
                category.entities.any { entity ->
                    entity.translations?.language == userLanguage
                }
            }
        }
    }

    // Display categories once fetched
    categories?.let { categoryList ->
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(categoryList) { category ->
                // Display each category
                CategorySection(category = category, userLanguage = userLanguage)
            }
        }
    } ?: run {
        // Loading state
        Text("Loading categories...", color = Color.White)
    }
}

@Composable
fun CategorySection(category: HomeCategoryResponse, userLanguage: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Display the category name
        Text(
            text = category.categoryName,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 10.dp, top = 10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter entities based on the user language and display thumbnails
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Only display entities that have a translation in the current language
            items(category.entities.filter { it.translations?.language == userLanguage }) { entity ->
                val thumbnailUrl = entity.thumbnail ?: "" // Provide an empty string or a placeholder for null values
                if (thumbnailUrl.isNotEmpty()) {
                    ThumbnailItem(thumbnailUrl)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}


@Composable
fun ThumbnailItem(thumbnailUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(thumbnailUrl),
        contentDescription = "Thumbnail",
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .padding(start = 10.dp, top = 10.dp)
    )
}
