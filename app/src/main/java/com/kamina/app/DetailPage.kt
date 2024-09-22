package com.kamina.app

import Navbar
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.*
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
    var video by remember { mutableStateOf<WatchPageEpisode?>(null) } // Updated to use WatchPageEpisode
    val coroutineScope = rememberCoroutineScope()

    // Fetch entity details, cast, suggestions, and seasons if series
    LaunchedEffect(entityId) {
        coroutineScope.launch {
            Log.d("DetailPage", "Fetching entity details for entityId: $entityId")
            entity = fetchEntityDetails(entityId)
            Log.d("DetailPage", "Fetched entity: $entity")

            cast = fetchCast(entityId) ?: emptyList()
            suggestions = fetchSuggestions(entityId) ?: emptyList()

            if (entity?.isMovie == 1) {
                Log.d("DetailPage", "Fetching movie details for entityId: $entityId")
                video = getMovieDetails(entityId)
                Log.d("DetailPage", "Fetched movie: $video")
            } else {
                Log.d("DetailPage", "Fetching episode for entityId: $entityId, season: 1, episode: 1")
                video = fetchWatchPageEpisode(entityId, season = 1, episode = 1)
                Log.d("DetailPage", "Fetched video: $video")
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

                    // Add PlayButton under logo/name, pass the userId dynamically
                    PlayButton(
                        entityId = entityDetail.id,       // Pass the correct entity ID
                        isMovie = entityDetail.isMovie,   // Pass whether it's a movie or series
                        videoFilePath = video?.filePath,  // Ensure video is fetched correctly, then pass its filePath
                        userId = userId                   // Pass the actual user ID
                    )

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
                            if (entityDetail.isMovie == 0 && seasons.isNotEmpty()) {
                                DetailPageEpisodesTab(
                                    entityDetail = entityDetail,
                                    seasons = seasons,
                                    episodes = episodes,
                                    onSeasonSelected = { season ->
                                        coroutineScope.launch {
                                            selectedSeason = season
                                            // Fetch episodes for the selected season
                                            episodes = fetchEpisodes(entityId, season) ?: emptyList()
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
