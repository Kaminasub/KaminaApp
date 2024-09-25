package com.kamina.app

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.CategoryResponse
import com.kamina.app.api.EntityResponse
import com.kamina.app.api.ThumbnailData
import com.kamina.app.api.fetchCategories
import com.kamina.app.api.fetchContinueWatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CombinedSections(userId: Int, entities: List<EntityResponse>) {
    var continueWatchingThumbnails by remember { mutableStateOf<List<ThumbnailData>?>(null) }
    var categories by remember { mutableStateOf<List<CategoryResponse>?>(null) }
    var isLoadingContinue by remember { mutableStateOf(true) }
    var isLoadingCategories by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    var currentIndex by remember { mutableStateOf(0) }
    var isUserInteracting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch "Continue Watching" content and filter only those with status = 1 (following)
    LaunchedEffect(userId) {
        fetchContinueWatching(userId.toString()) { result ->
            continueWatchingThumbnails = result?.filter { it.status == 1 }  // Filter to show only following status
            isLoadingContinue = false
        }
    }

    // Fetch categories
    LaunchedEffect(Unit) {
        fetchCategories { result ->
            categories = result
            isLoadingCategories = false
        }
    }

    // Handle automatic carousel scrolling
    LaunchedEffect(entities) {
        if (entities.isNotEmpty()) {
            coroutineScope.launch {
                while (true) {
                    delay(7000L)
                    if (!isUserInteracting) {
                        currentIndex = (currentIndex + 1) % entities.size
                        listState.animateScrollToItem(currentIndex)
                    }
                }
            }
        }
    }

    // Detect user interaction in the carousel
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isUserInteracting = true
        } else {
            coroutineScope.launch {
                delay(5000L)
                isUserInteracting = false
            }
            currentIndex = listState.firstVisibleItemIndex
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp)
    ) {
        // Section: Carousel for Entities
        item {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(entities) { index, entity ->
                    val nextEntity = if (index + 1 < entities.size) entities[index + 1] else null
                    CarouselItem(entity = entity, nextEntity = nextEntity, userId = userId)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Continue Watching (filtered to show only following status)
        item {
            if (!continueWatchingThumbnails.isNullOrEmpty()) {
                Text(
                    text = "Continue Watching",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(continueWatchingThumbnails!!) { thumbnail ->
                        CombinedThumbnailItem(thumbnail.thumbnail, thumbnail.id, userId) // Pass entityId and userId
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else if (isLoadingContinue) {
                Text(
                    text = "Loading...",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Section: Categories
        if (!categories.isNullOrEmpty()) {
            items(categories!!) { category ->
                CombinedCategorySection(category = category, userId = userId)
            }
        } else if (isLoadingCategories) {
            item {
                Text(
                    text = "Loading categories...",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}


// CarouselItem with click action to navigate to DetailPageActivity
@Composable
fun CarouselItem(entity: EntityResponse, nextEntity: EntityResponse?, userId: Int) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .width(400.dp)
            .padding(end = 10.dp)
            .clickable {
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("entityId", entity.id)
                    putExtra("userId", userId) // Pass the userId as Int
                }
                ContextCompat.startActivity(context, intent, null)
            }
    ) {
        entity.pic?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Entity Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(end = 17.5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            if (entity.logo != null && entity.logo.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(entity.logo),
                    contentDescription = "Entity Logo",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Text(
                    text = entity.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// CombinedThumbnailItem with click action to navigate to DetailPageActivity
@Composable
fun CombinedThumbnailItem(thumbnailUrl: String, entityId: Int, userId: Int) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Image(
        painter = rememberAsyncImagePainter(thumbnailUrl),
        contentDescription = "Thumbnail",
        modifier = Modifier
            .width(186.dp)
            .height(280.dp)
            .padding(end = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable {
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("entityId", entityId) // Pass the correct entityId
                    putExtra("userId", userId) // Pass the userId as Int
                }
                Log.d("CombinedThumbnailItem", "Thumbnail clicked with entityId: $entityId, userId: $userId")
                ContextCompat.startActivity(context, intent, null)
            },
        contentScale = ContentScale.Crop
    )
}

// CombinedCategorySection
@Composable
fun CombinedCategorySection(category: CategoryResponse, userId: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category.categoryName,
            color = Color.White,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(category.entities) { entity ->
                CombinedThumbnailItem(entity.thumbnail, entity.id, userId) // Pass entityId and userId
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}