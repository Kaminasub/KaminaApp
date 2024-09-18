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
import com.kamina.app.api.CategoryResponse
import com.kamina.app.api.fetchCategories

@Composable
fun CategoriesSection() {
    var categories by remember { mutableStateOf<List<CategoryResponse>?>(null) }

    // Fetch categories from the API
    LaunchedEffect(Unit) {
        fetchCategories { result ->
            categories = result
        }
    }

    // Display categories once fetched
    categories?.let { categoryList ->
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(categoryList) { category ->
                // Display each category
                CategorySection(category = category)
            }
        }
    } ?: run {
        // Loading state
        Text("Loading categories...", color = Color.White)
    }
}

@Composable
fun CategorySection(category: CategoryResponse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Display the category name
        Text(
            text = category.categoryName,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 10.dp, top = 10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Display thumbnails in a horizontal scroll
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(category.thumbnails) { thumbnailUrl ->
                ThumbnailItem(thumbnailUrl)
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
