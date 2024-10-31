package com.kamina.app

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.CarouselEntityResponse
import com.kamina.app.api.HomeCategoryResponse
import com.kamina.app.api.ThumbnailData
import com.kamina.app.api.fetchCarouselEntities
import com.kamina.app.api.fetchContinueWatching
import com.kamina.app.api.fetchHomeCategories
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CombinedSections(userId: Int, userLanguage: String) {
    var continueWatchingThumbnails by remember { mutableStateOf<List<ThumbnailData>?>(null) }
    var categories by remember { mutableStateOf<List<HomeCategoryResponse>?>(null) }
    var carouselEntities by remember { mutableStateOf<List<CarouselEntityResponse>?>(null) }
    var isLoadingContinue by remember { mutableStateOf(true) }
    var isLoadingCategories by remember { mutableStateOf(true) }
    var isLoadingCarousel by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    var currentIndex by remember { mutableStateOf(0) }
    var isUserInteracting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch "Continue Watching" content using the new ContinueApiService
    LaunchedEffect(userId, userLanguage) {  // Include userLanguage in LaunchedEffect
        Log.d("CombinedSections", "Fetching continue watching for user: $userId and language: $userLanguage")
        fetchContinueWatching(userId.toString(), userLanguage) { result ->  // Pass userLanguage here
            if (result != null) {
                Log.d("CombinedSections", "Fetched ${result.size} items for continue watching.")
                // Filter by non-null thumbnails and names
                continueWatchingThumbnails = result.filter {
                    it.status == 1 && !it.name.isNullOrEmpty() && !it.thumbnail.isNullOrEmpty()
                }
                Log.d("CombinedSections", "Filtered to ${continueWatchingThumbnails?.size} items with valid thumbnails.")
            } else {
                Log.d("CombinedSections", "Failed to fetch continue watching data.")
            }
            isLoadingContinue = false
        }
    }

    // Fetch categories from the home API with the userLanguage
    LaunchedEffect(userLanguage) {
        fetchHomeCategories(language = userLanguage) { result ->
            categories = result?.filter { category ->
                category.entities.any { entity ->
                    entity.translations?.language == userLanguage
                }
            }
            isLoadingCategories = false
        }
    }

    // Fetch carousel entities
    LaunchedEffect(userLanguage) {
        fetchCarouselEntities(language = userLanguage) { result ->
            carouselEntities = result
            isLoadingCarousel = false
        }
    }

    // Automatic scrolling for the carousel
    LaunchedEffect(carouselEntities) {
        if (!carouselEntities.isNullOrEmpty()) {
            coroutineScope.launch {
                while (true) {
                    delay(5000L) // Delay for 5 seconds
                    if (!isUserInteracting) {
                        currentIndex = (currentIndex + 1) % carouselEntities!!.size
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
                delay(3000L) // Delay before re-enabling auto-scrolling
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
            if (!carouselEntities.isNullOrEmpty()) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(carouselEntities!!) { entity ->
                        CarouselItem(entity = entity, userId = userId, userLanguage = userLanguage)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else if (isLoadingCarousel) {
                Text("Loading carousel...", color = Color.White)
            }
        }

        // Section: Continue Watching
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
                        CombinedThumbnailItem(thumbnail.thumbnail ?: "", thumbnail.id, userId, userLanguage)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else if (isLoadingContinue) {
                Text("Loading...", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(10.dp))
            }
        }

        // Section: Categories
        if (!categories.isNullOrEmpty()) {
            items(categories!!) { category ->
                CombinedCategorySection(category = category, userId = userId, userLanguage = userLanguage)
            }
        } else if (isLoadingCategories) {
            item {
                Text("Loading categories...", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}



// CarouselItem with click action to navigate to DetailPageActivity
@Composable
fun CarouselItem(entity: CarouselEntityResponse, userId: Int, userLanguage: String) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(400.dp)
            .padding(end = 10.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
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
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("entityId", entity.id)
                    putExtra("userId", userId)
                    putExtra("userLanguage", userLanguage)
                }
                context.startActivity(intent)
            }
    ) {
        // Display the entity's main picture
        entity.pic?.let { pic ->
            Image(
                painter = rememberAsyncImagePainter(pic),
                contentDescription = "Carousel Thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(end = 17.5.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // Overlay the entity's logo or name on top of the image
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            if (!entity.logo.isNullOrEmpty()) {
                // Display the logo if available
                Image(
                    painter = rememberAsyncImagePainter(entity.logo),
                    contentDescription = "Entity Logo",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                // Fallback to name if the logo is not available
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



// CombinedCategorySection
@Composable
fun CombinedCategorySection(category: HomeCategoryResponse, userId: Int, userLanguage: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Display the category name
        Text(
            text = category.categoryName,
            color = Color.White,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Display category thumbnails in a horizontal scroll
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Filter entities based on the existence of a translation for the current language
            val filteredEntities = category.entities.filter { entity ->
                val translation = entity.translations?.language == userLanguage
                if (translation) {
                    Log.d("CombinedSections", "Translation found for entityId: ${entity.id} in language: $userLanguage")
                    true
                } else {
                    Log.d("CombinedSections", "No translation found for entityId: ${entity.id} in language: $userLanguage")
                    false
                }
            }

            // Display filtered entities
            items(filteredEntities) { entity ->
                // Use the entity's thumbnail by default
                val thumbnailUrl = entity.thumbnail ?: ""

                if (!thumbnailUrl.isNullOrEmpty()) {
                    CombinedThumbnailItem(
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

// CombinedThumbnailItem with click action to navigate to DetailPageActivity
@Composable
fun CombinedThumbnailItem(thumbnailUrl: String, entityId: Int, userId: Int, userLanguage: String) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }

    Image(
        painter = rememberAsyncImagePainter(thumbnailUrl),
        contentDescription = "Thumbnail",
        modifier = Modifier
            .width(186.dp)
            .height(280.dp)
            .padding(end = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .then(
                if (isFocused) {
                    Modifier.border(3.dp, Color.White, RoundedCornerShape(12.dp))
                } else {
                    Modifier.border(0.dp, Color.Transparent, RoundedCornerShape(12.dp))
                }
            )
            .clickable {
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("entityId", entityId)
                    putExtra("userId", userId)
                    putExtra("userLanguage", userLanguage)
                }
                context.startActivity(intent)
            },
        contentScale = ContentScale.Crop
    )
}
