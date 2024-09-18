package com.kamina.app

import Navbar
import android.content.Intent
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
import com.kamina.app.api.fetchCast
import com.kamina.app.api.fetchEntityDetails
import com.kamina.app.api.fetchEpisodes
import com.kamina.app.api.fetchSeasons
import com.kamina.app.api.fetchSuggestions
import kotlinx.coroutines.launch


@Composable
fun DetailPageScreen(entityId: Int) {
    val navController = rememberNavController() // Create a dummy NavController
    var entity by remember { mutableStateOf<EntityDetail?>(null) }
    var cast by remember { mutableStateOf<List<CastMember>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
    var seasons by remember { mutableStateOf<List<Season>>(emptyList()) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<Int?>(null) }
    var activeTab by remember { mutableStateOf("EPISODES") }
    val coroutineScope = rememberCoroutineScope()

    // Fetch entity details, cast, suggestions, and seasons if series
    LaunchedEffect(entityId) {
        entity = fetchEntityDetails(entityId)
        cast = fetchCast(entityId) ?: emptyList()
        suggestions = fetchSuggestions(entityId) ?: emptyList()

        if (entity?.isMovie == 0) {
            seasons = fetchSeasons(entityId) ?: emptyList()
        }
    }

    Scaffold(
        topBar = { Navbar(navController = navController, avatarChanged = false) },  // Reuse the Navbar
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

                    // Center the logo or name
                    Box(
                        modifier = Modifier.fillMaxWidth(),  // Ensures the Box takes the full width
                        contentAlignment = Alignment.Center  // Centers content within the Box
                    ) {
                        entityDetail.logo?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .height(100.dp)
                            )
                        } ?: Text(
                            text = entityDetail.name,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(8.dp)
                        )
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
                            Text("details")
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
                                            episodes = fetchEpisodes(entityId, season) ?: emptyList()
                                        }
                                    }
                                )
                            }
                        }
                        "SUGGESTIONS" -> {
                            val context = LocalContext.current  // Make sure the context is defined in the composable

                            LazyRow {
                                items(suggestions) { suggestion ->
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(suggestion.thumbnail),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(188.dp)  // Consistent width
                                                .height(280.dp) // Consistent height
                                                .padding(start = 7.dp)  // Reduced margin to match categories
                                                .clip(RoundedCornerShape(12.dp))  // 12dp radius for carousel items
                                                .background(Color.White)
                                                .clickable {
                                                    // Navigate to DetailPageActivity when thumbnail is clicked
                                                    val intent = Intent(context, DetailPageActivity::class.java).apply {
                                                        putExtra("entityId", suggestion.id)  // Pass the suggestion.id to DetailPageActivity
                                                    }
                                                    context.startActivity(intent)
                                                },
                                            contentScale = ContentScale.Crop  // Crop the image to fit the container without distortion
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
                                        // Display actor's profile picture
                                        val profileImageUrl = actor.profile_path?.let {
                                            "https://media.themoviedb.org/t/p/w300_and_h450_bestv2${it}"
                                        } ?: "https://www.themoviedb.org/assets/2/v4/glyphicons/basic/glyphicons-basic-4-user-grey-d8fe957375e70239d6abdd549fd7568c89281b2179b5f4470e2e12895792dfa5.svg"

                                        Image(
                                            painter = rememberAsyncImagePainter(profileImageUrl),
                                            contentDescription = actor.name,
                                            modifier = Modifier
                                                .width(94.dp)  // Consistent width
                                                .height(140.dp) // Consistent height
                                                .padding(start = 7.dp)  // Reduced margin to match categories
                                                .clip(RoundedCornerShape(12.dp))  // 12dp radius for carousel items
                                                .background(Color.White),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Display actor's name and character name
                                        Text(
                                            text = actor.name.replace(" ", "\n"), // Replace spaces with new lines
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = actor.character_name.replace(" ", "\n"), // Optionally do the same for character name
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
                                    // Display the name and resumen from entity details
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
                                // Display loading or error message if entity details are not available
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

