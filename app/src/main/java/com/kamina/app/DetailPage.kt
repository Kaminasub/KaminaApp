package com.kamina.app

import Navbar
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.CastMember
import com.kamina.app.api.EntityDetail
import com.kamina.app.api.Episode
import com.kamina.app.api.Season
import com.kamina.app.api.Suggestion
import com.kamina.app.api.UserProgress
import com.kamina.app.api.WatchPageEpisode
import com.kamina.app.api.fetchCast
import com.kamina.app.api.fetchEntityDetails
import com.kamina.app.api.fetchEpisodes
import com.kamina.app.api.fetchSeasons
import com.kamina.app.api.fetchSuggestions
import com.kamina.app.api.fetchUserEntityStatus
import com.kamina.app.api.fetchUserProgress
import com.kamina.app.api.getMovieDetails
import com.kamina.app.api.getStatusText
import com.kamina.app.api.updateUserStatus
import kotlinx.coroutines.launch

@Composable
fun DetailPageScreen(entityId: Int, userId: Int) {
    val navController = rememberNavController()
    var entity by remember { mutableStateOf<EntityDetail?>(null) }
    var cast by remember { mutableStateOf<List<CastMember>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
    var seasons by remember { mutableStateOf<List<Season>>(emptyList()) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<Int?>(null) }
    var activeTab by remember { mutableStateOf("EPISODES") }
    var video by remember { mutableStateOf<WatchPageEpisode?>(null) }
    var selectedStatus by remember { mutableStateOf<Int?>(null) }  // Nullable to avoid premature defaulting
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(1 to "Following", 2 to "Completed")
    val coroutineScope = rememberCoroutineScope()
    var currentEpisode by remember { mutableStateOf<Episode?>(null) }
    var userProgress by remember { mutableStateOf<UserProgress?>(null) }

    // Fetch entity details, cast, suggestions, seasons, and status if series
    LaunchedEffect(entityId) {
        coroutineScope.launch {
            // Fetch entity details, cast, and suggestions
            entity = fetchEntityDetails(entityId)
            cast = fetchCast(entityId) ?: emptyList()
            suggestions = fetchSuggestions(entityId) ?: emptyList()

            if (entity?.isMovie == 1) {
                video = getMovieDetails(entityId)
                currentEpisode = null // No episodes for movies
            } else {
                seasons = fetchSeasons(entityId) ?: emptyList()

                if (seasons.isNotEmpty()) {
                    selectedSeason = seasons.first().season
                    episodes = fetchEpisodes(entityId, selectedSeason!!) ?: emptyList()

                    // Fetch user progress for series
                    userProgress = fetchUserProgress(userId, entityId)
                    userProgress?.let { progress ->
                        selectedSeason = progress.currentSeason ?: seasons.firstOrNull()?.season
                        episodes = fetchEpisodes(entityId, selectedSeason!!) ?: emptyList()

                        currentEpisode = episodes.firstOrNull { it.episode == progress.currentEpisode }
                            ?: episodes.firstOrNull() // Default to first episode if no progress found
                    } ?: run {
                        currentEpisode = episodes.firstOrNull() // Default to the first episode
                    }
                }

                // Log to help debug episodes fetching
                Log.d("DetailPage", "Fetched episodes: $episodes, currentEpisode: $currentEpisode")
            }

            // Fetch current status for the user and entity
            fetchUserEntityStatus(userId, entityId) { result ->
                selectedStatus = result?.status ?: 0  // Default to 0 if no status is found
                Log.d("DetailPage", "Fetched status: $selectedStatus")
            }
        }
    }

    Scaffold(
        topBar = { Navbar(navController = navController, avatarChanged = false) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(2.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.Top
            ) {
                entity?.let { entityDetail ->
                    // Display wall image
                    entityDetail.wall?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Display logo or name
                    entityDetail.logo?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Logo",
                            modifier = Modifier.height(100.dp)
                        )
                    } ?: Text(
                        text = entityDetail.name,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(8.dp)
                    )

                    // Display episode information before the PlayButton (only for series)
                    if (entityDetail.isMovie == 0) {
                        currentEpisode?.let { episode ->
                            Text(
                                text = "S${episode.season}E${episode.episode}: ${episode.title}",
                                fontSize = 18.sp,
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // PlayButton and Status Dropdown in the same row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // PlayButton on the left
                        PlayButton(
                            entityId = entityDetail.id,
                            isMovie = entityDetail.isMovie,
                            videoFilePath = video?.filePath,
                            userId = userId,
                            modifier = Modifier.weight(1f)
                        )

                        // Status Dropdown on the right
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Button(onClick = { expanded = !expanded }) {
                                Text(text = "Status: ${getStatusText(selectedStatus ?: 0)}")
                            }

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
                    }

                    // Tabs Section
                    TabRow(selectedTabIndex = when (activeTab) {
                        "EPISODES" -> 0
                        "SUGGESTIONS" -> 1
                        "CAST" -> 2
                        "DETAILS" -> 3
                        else -> 0
                    }) {
                        if (entityDetail.isMovie == 0) {
                            Tab(selected = activeTab == "EPISODES", onClick = { activeTab = "EPISODES" }) {
                                Text("Episodes")
                            }
                        }
                        Tab(selected = activeTab == "SUGGESTIONS", onClick = { activeTab = "SUGGESTIONS" }) {
                            Text("Suggestions")
                        }
                        Tab(selected = activeTab == "CAST", onClick = { activeTab = "CAST" }) {
                            Text("Cast")
                        }
                        Tab(selected = activeTab == "DETAILS", onClick = { activeTab = "DETAILS" }) {
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
                                            seasons = fetchSeasons(entityId) ?: emptyList()
                                            Log.d("DetailPage", "Fetched seasons: $seasons")
                                        }
                                    }
                                }

                                // Fetch episodes when a season is selected
                                selectedSeason?.let { season ->
                                    coroutineScope.launch {
                                        Log.d("DetailPage", "Fetching episodes for entityId: $entityId, season: $season")
                                        episodes = fetchEpisodes(entityId, season) ?: emptyList()
                                        Log.d("DetailPage", "Fetched episodes: $episodes")
                                    }
                                }

                                // Render the Episodes tab with seasons and episodes, passing userId
                                DetailPageEpisodesTab(
                                    entityDetail = entityDetail,
                                    seasons = seasons,
                                    episodes = episodes,
                                    userId = userId,
                                    onSeasonSelected = { season ->
                                        coroutineScope.launch {
                                            selectedSeason = season
                                            Log.d("DetailPage", "Season selected: $season. Fetching episodes for entityId: $entityId")
                                            episodes = fetchEpisodes(entityId, season) ?: emptyList()
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
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(suggestion.thumbnail),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(188.dp)
                                                .height(280.dp)
                                                .padding(start = 7.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .clickable {
                                                    val intent = Intent(context, DetailPageActivity::class.java).apply {
                                                        putExtra("entityId", suggestion.id)
                                                        putExtra("userId", userId)  // Pass userId to suggestions
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
                                    Column(
                                        modifier = Modifier.padding(8.dp)
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
                } ?: run {
                    Text("Loading...", fontSize = 20.sp)
                }
            }
        }
    )
}
