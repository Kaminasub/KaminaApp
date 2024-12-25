package com.kamina.app


import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.CastMember
import com.kamina.app.api.EntityDetail
import com.kamina.app.api.Episode
import com.kamina.app.api.Season
import com.kamina.app.api.Suggestion
import com.kamina.app.api.UserApiHelper
import com.kamina.app.api.UserProgress
import com.kamina.app.api.WatchPageEpisode
import com.kamina.app.api.fetchCast
import com.kamina.app.api.fetchEntityDetails
import com.kamina.app.api.fetchEpisodes
import com.kamina.app.api.fetchNextEpisode
import com.kamina.app.api.fetchSeasons
import com.kamina.app.api.fetchSuggestions
import com.kamina.app.api.fetchUserEntityStatus
import com.kamina.app.api.fetchUserProgress
import com.kamina.app.api.fetchWatchPageEpisode
import com.kamina.app.api.getMovieDetails
import com.kamina.app.api.getStatusText
import com.kamina.app.api.updateUserStatus
import com.kamina.app.ui.components.CustomButton
import com.kamina.app.ui.components.UserDropdownMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DetailPageScreen(entityId: Int, userId: Int, userLanguage: String) {
    val context = LocalContext.current
    var userIconUrl by remember { mutableStateOf<String?>(null) }

    // Log to check the language being passed to the screen
    Log.d("DetailPage", "Received entityId: $entityId, userId: $userId, userLanguage: $userLanguage")

    // Fetch user icon if userId is not null
    LaunchedEffect(userId) {
        UserApiHelper.fetchUserIcon(userId.toString()) { iconUrl ->
            userIconUrl = iconUrl
        }
    }

    val painter = rememberAsyncImagePainter(userIconUrl ?: "")
    var isFocused by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    var entity by remember { mutableStateOf<EntityDetail?>(null) }
    var cast by remember { mutableStateOf<List<CastMember>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
    var seasons by remember { mutableStateOf<List<Season>>(emptyList()) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<Int?>(null) }
    var activeTab by remember { mutableStateOf("EPISODES") }
    var video by remember { mutableStateOf<WatchPageEpisode?>(null) }
    var selectedStatus by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(1 to "Following", 2 to "Completed")
    val coroutineScope = rememberCoroutineScope()
    var currentEpisode by remember { mutableStateOf<Episode?>(null) }
    var userProgress by remember { mutableStateOf<UserProgress?>(null) }

    // Fetch entity details, cast, suggestions, seasons, and status if series
    LaunchedEffect(entityId, userLanguage) {
        coroutineScope.launch {
            Log.d("DetailPage", "Fetching entity details for entityId: $entityId, userLanguage: $userLanguage")

            // Fetch entity details with language parameter
            entity = fetchEntityDetails(entityId, userLanguage)

            // Fetch cast (same across all languages)
            cast = fetchCast(entityId) ?: emptyList()

            // Fetch suggestions with language parameter
            suggestions = fetchSuggestions(entityId, userLanguage) ?: emptyList()

            if (entity?.isMovie == 1) {
                Log.d("DetailPage", "Fetching movie details for entityId: $entityId, language: $userLanguage")
                video = getMovieDetails(entityId, userLanguage)
                currentEpisode = null
                activeTab = "SUGGESTIONS"
            } else {
                Log.d("DetailPage", "Fetching seasons for entityId: $entityId")
                seasons = fetchSeasons(entityId, userLanguage) ?: emptyList()

                if (seasons.isNotEmpty()) {
                    selectedSeason = seasons.first().season
                    selectedSeason?.let { season ->
                        Log.d("DetailPage", "Fetching episodes for season $season, entityId: $entityId, language: $userLanguage")
                        episodes = fetchEpisodes(entityId, season, userLanguage) ?: emptyList()
                    }

                    userProgress = fetchUserProgress(userId, entityId)
                    userProgress?.let { progress ->
                        selectedSeason = progress.currentSeason ?: seasons.firstOrNull()?.season
                        selectedSeason?.let { season ->
                            Log.d("DetailPage", "Fetching episodes based on user progress for season $season, entityId: $entityId, language: $userLanguage")
                            episodes = fetchEpisodes(entityId, season, userLanguage) ?: emptyList()
                        }
                    } ?: run {
                        currentEpisode = episodes.firstOrNull()
                    }
                }

                activeTab = "EPISODES"
            }

            fetchUserEntityStatus(userId, entityId) { result ->
                selectedStatus = result?.status ?: 0
                Log.d("DetailPage", "Fetched status: $selectedStatus")
            }
        }
    }

    // Additional logging for episodes fetching process
    //coroutineScope.launch {
    //    selectedSeason?.let { season ->
    //        Log.d("DetailPage", "Manually fetching episodes for entityId: $entityId, season: $season, language: $userLanguage")
    //        episodes = fetchEpisodes(entityId, season, userLanguage) ?: emptyList()
    //        Log.d("DetailPage", "Manually fetched episodes for season $season: $episodes")
    //    } ?: run {
    //        Log.e("DetailPage", "Selected season is null, cannot fetch episodes.")
    //    }
    //}

    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF182459), // #663dff at 0%
                                Color(0xFF14143C),
                                Color(0xFF000000)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(x = 1000f, y = -1000f)  // Adjust this to control the gradient angle
                        )
                    )
            ) {
                // Main content for DetailPage
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 0.dp) // Adjust padding to avoid overlap with the user icon
                ) {
                    entity?.let { entityDetail ->
                        // Wall Image Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) // Adjust as necessary to account for both wall and logo
                        ) {
                            // Display wall image
                            entityDetail.wall?.let {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp) // Wall image height
                                        .clip(RoundedCornerShape(0.dp))
                                        .background(Color.White),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // User Icon in the top right corner, layered on top of the wall image
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = "User Icon",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .onFocusChanged { focusState ->
                                            isFocused =
                                                focusState.isFocused  // Update the state when focus changes
                                        }
                                        .focusable()  // Make the icon focusable
                                        .then(
                                            if (isFocused) {
                                                Modifier
                                                    .border(
                                                        width = 3.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                                    .padding(3.dp)  // Optional: to prevent the border from overlapping with the icon
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clip(CircleShape)  // Make sure the icon and selector are circular
                                        .clickable {
                                            showDropdownMenu =
                                                true // Show the dropdown menu when clicked
                                        }
                                )

                                // Align DropdownMenu under User Icon
                                UserDropdownMenu(
                                    expanded = showDropdownMenu,
                                    onDismissRequest = { showDropdownMenu = false },
                                    context = context
                                )
                            }

                            // Display logo or name, and episode information, overlaid on the bottom part of the wall image
                            // Display logo or name, and episode information, overlaid on the bottom part of the wall image
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp) // Padding to add space from the wall image bottom
                            ) {
                                Column {
                                    // Display logo if it exists, otherwise display the name
                                    if (!entityDetail.logo.isNullOrEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(entityDetail.logo),
                                            contentDescription = "Logo",
                                            modifier = Modifier
                                                .height(100.dp) // Logo height
                                                .offset(y = 5.dp) // Offset the logo to make it overlap with the wall
                                        )
                                    } else {
                                        Text(
                                            text = entityDetail.name,
                                            fontSize = 24.sp,
                                            color = Color.White // Ensure text is visible
                                        )
                                    }

                                    // Display episode information (only for series)
                                    if (entityDetail.isMovie == 0) {
                                        var nextEpisode by remember { mutableStateOf<Episode?>(null) } // State to hold the next episode

                                        // Fetch the next episode based on user progress
                                        LaunchedEffect(userProgress) {
                                            if (userProgress != null) {
                                                userProgress?.let { progress ->
                                                    val nextEpisodeData = fetchNextEpisode(
                                                        entityId = entityDetail.id,
                                                        season = progress.currentSeason ?: 1,
                                                        episode = progress.currentEpisode ?: 0, // Assume progress starts with episode 0 for S1E1
                                                        language = userLanguage
                                                    )

                                                    // Map WatchPageEpisode to Episode if next episode is found
                                                    nextEpisode = nextEpisodeData?.let { watchPageEpisode ->
                                                        Episode(
                                                            season = watchPageEpisode.season,
                                                            episode = watchPageEpisode.episode,
                                                            title = watchPageEpisode.title,
                                                            description = watchPageEpisode.description,
                                                            miniatura = "" // Set empty or a valid placeholder since Episode has `miniatura`
                                                        )
                                                    } ?: currentEpisode // Fallback to currentEpisode
                                                }
                                            } else {
                                                // If no user progress, explicitly fetch the first episode (S1E1)
                                                val firstEpisode = fetchWatchPageEpisode(
                                                    entityId = entityDetail.id,
                                                    season = 1,
                                                    episode = 1,
                                                    language = userLanguage
                                                )

                                                nextEpisode = firstEpisode?.let { watchPageEpisode ->
                                                    Episode(
                                                        season = watchPageEpisode.season,
                                                        episode = watchPageEpisode.episode,
                                                        title = watchPageEpisode.title,
                                                        description = watchPageEpisode.description,
                                                        miniatura = "" // Set empty or a valid placeholder since Episode has `miniatura`
                                                    )
                                                }
                                            }
                                        }

                                        // Display the next episode information
                                        nextEpisode?.let { episode ->
                                            Text(
                                                text = "S${episode.season}E${episode.episode}: ${episode.title}",
                                                fontSize = 18.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        } ?: run {
                                            // Show fallback text if no episode information is available
                                            Text(
                                                text = "No episode information available",
                                                fontSize = 18.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }

                                }
                            }


                            // PlayButton and Status Dropdown over the wall image
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically, // Ensure both buttons are aligned vertically to the center of the Row
                                horizontalArrangement = Arrangement.spacedBy(16.dp) // Spacing between the buttons
                            ) {
                                // PlayButton on the left side of the bottom
                                PlayButton(
                                    entityId = entityDetail.id,
                                    isMovie = entityDetail.isMovie,
                                    videoFilePath = video?.filePath,
                                    userId = userId,
                                    userLanguage = userLanguage,  // Pass userLanguage here
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically) // Ensure the PlayButton aligns to the center vertically within the Row
                                )


                                // Status Button on the right side of the bottom
                                CustomButton(
                                    text = "Status: ${getStatusText(selectedStatus ?: 0)}",
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically) // Ensure the CustomButton aligns to the center vertically within the Row
                                )
                            }

                            // Status Dropdown Menu
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                statusOptions.forEach { (statusCode, statusText) ->
                                    DropdownMenuItem(
                                        text = { Text(text = statusText) },
                                        onClick = {
                                            selectedStatus = statusCode
                                            expanded = false
                                            // Call the API to update user status
                                            updateUserStatus(userId, entityDetail.id, statusCode)
                                        }
                                    )
                                }
                            }
                        }

                        // Tabs Section (Episodes, Suggestions, Cast, Details)
                        Spacer(modifier = Modifier.height(0.dp)) // This will help reduce the gap
                        TabRow(
                            selectedTabIndex = when (activeTab) {
                                "EPISODES" -> 0
                                "SUGGESTIONS" -> 1
                                "CAST" -> 2
                                "DETAILS" -> 3
                                else -> 0 // Default to 0 if something goes wrong
                            },
                            modifier = Modifier.padding(top = 0.dp)
                        ) {
                            if (entityDetail.isMovie == 0) {
                                Tab(
                                    selected = activeTab == "EPISODES",
                                    onClick = { activeTab = "EPISODES" }
                                ) {
                                    Text("Episodes")
                                }
                            }
                            Tab(
                                selected = activeTab == "SUGGESTIONS",
                                onClick = { activeTab = "SUGGESTIONS" }
                            ) {
                                Text("Suggestions")
                            }
                            Tab(
                                selected = activeTab == "CAST",
                                onClick = { activeTab = "CAST" }
                            ) {
                                Text("Cast")
                            }
                            Tab(
                                selected = activeTab == "DETAILS",
                                onClick = { activeTab = "DETAILS" }
                            ) {
                                Text("Details")
                            }
                        }

                        // Tab Content
                        when (activeTab) {
                            "EPISODES" -> {
                                if (entityDetail.isMovie == 0) {
                                    // Fetch seasons if needed
                                    LaunchedEffect(entityId) {
                                        if (seasons.isEmpty()) {
                                            coroutineScope.launch {
                                                Log.d("DetailPage", "Fetching seasons for entityId: $entityId")
                                                seasons = fetchSeasons(entityId, userLanguage) ?: emptyList()
                                                Log.d("DetailPage", "Fetched seasons: $seasons")
                                            }
                                        }
                                    }

                                    // Fetch episodes when a season is selected
                                    selectedSeason?.let { season ->
                                        coroutineScope.launch {
                                            Log.d("DetailPage", "Fetching episodes for entityId: $entityId, season: $season")
                                            episodes = fetchEpisodes(entityId, season, userLanguage) ?: emptyList() // Fetch episodes with language
                                            Log.d("DetailPage", "Fetched episodes: $episodes")
                                        }
                                    }

                                    // Render the Episodes tab with seasons and episodes, passing userId and userLanguage
                                    DetailPageEpisodesTab(
                                        entityDetail = entityDetail,
                                        seasons = seasons,
                                        episodes = episodes,
                                        userId = userId,
                                        userLanguage = userLanguage,  // Ensure userLanguage is passed here
                                        onSeasonSelected = { season ->
                                            coroutineScope.launch {
                                                selectedSeason = season
                                                Log.d("DetailPage", "Season selected: $season. Fetching episodes for entityId: $entityId")
                                                episodes = fetchEpisodes(entityId, season, userLanguage) ?: emptyList()  // Ensure userLanguage is passed here
                                                Log.d("DetailPage", "Fetched episodes for season $season: $episodes")
                                            }
                                        }
                                    )
                                }
                            }

                            "SUGGESTIONS" -> {
                                val context = LocalContext.current

                                LazyRow {
                                    items(suggestions) { suggestion ->
                                        var isFocused by remember { mutableStateOf(false) } // Track focus state for each item individually

                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(suggestion.thumbnail),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .width(180.dp)
                                                    .height(280.dp)
                                                    .padding(start = 7.dp)
                                                    .clip(RoundedCornerShape(12.dp))  // Clip the image for rounded corners
                                                    .background(Color.White)
                                                    .onFocusChanged { focusState ->
                                                        isFocused = focusState.isFocused // Update focus state
                                                    }
                                                    .focusable()  // Make it focusable for TV navigation
                                                    .then(
                                                        if (isFocused) {
                                                            Modifier.border(
                                                                width = 3.dp,
                                                                color = Color.White,
                                                                shape = RoundedCornerShape(12.dp)
                                                            )
                                                        } else {
                                                            Modifier
                                                        }
                                                    )
                                                    .clickable {
                                                        val intent = Intent(context, DetailPageActivity::class.java).apply {
                                                            putExtra("entityId", suggestion.id)
                                                            putExtra("userId", userId)  // Pass userId to suggestions
                                                            putExtra("userLanguage", userLanguage)  // Pass language
                                                        }
                                                        context.startActivity(intent)
                                                    },
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }

                            "CAST" -> {
                                LazyRow {
                                    items(cast) { actor ->
                                        var isFocused by remember { mutableStateOf(false) } // Track focus state for each item individually

                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .onFocusChanged { focusState ->
                                                    isFocused = focusState.isFocused // Update focus state
                                                }
                                                .focusable()  // Make it focusable for TV navigation
                                                .then(
                                                    if (isFocused) {
                                                        Modifier.border(
                                                            width = 3.dp,
                                                            color = Color.White,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                        ) {
                                            val profileImageUrl = actor.profile_path?.let {
                                                "https://media.themoviedb.org/t/p/w300_and_h450_bestv2${it}"
                                            } ?: "https://www.themoviedb.org/assets/2/v4/glyphicons/basic/glyphicons-basic-4-user-grey-d8fe957375e70239d6abdd549fd7568c89281b2179b5f4470e2e12895792dfa5.svg"

                                            Image(
                                                painter = rememberAsyncImagePainter(profileImageUrl),
                                                contentDescription = actor.name,
                                                modifier = Modifier
                                                    .width(94.dp)
                                                    .height(140.dp)
                                                    .padding(start = 7.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.White),
                                                contentScale = ContentScale.Crop
                                            )

                                            Text(
                                                text = actor.name.replace(" ", "\n"),
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )

                                            Text(
                                                text = actor.character_name.replace(" ", "\n"),
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            "DETAILS" -> {
                                entity?.let { entityDetail ->
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = entityDetail.name,
                                            fontSize = 24.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = entityDetail.resumen,
                                            fontSize = 16.sp,
                                            color = Color.Gray
                                        )
                                    }
                                } ?: run {
                                    Text(
                                        text = "Loading details...",
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    Text("Loading...", fontSize = 20.sp)
                }
            }}
    )
}
