package com.kamina.app


import android.content.Intent
import androidx.benchmark.perfetto.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kamina.app.api.EntityDetail
import com.kamina.app.api.Episode
import com.kamina.app.api.Season
import androidx.compose.foundation.lazy.LazyColumn



@Composable
fun DetailPageEpisodesTab(
    entityDetail: EntityDetail,
    seasons: List<Season>,
    episodes: List<Episode>,
    userId: Int,  // Add userId as a parameter to pass from DetailPageScreen
    onSeasonSelected: (Int) -> Unit
) {
    var selectedSeason by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (entityDetail.isMovie == 0 && seasons.isNotEmpty()) {
        Column {
            Box(modifier = Modifier.padding(10.dp)) {
                Button(onClick = { expanded = !expanded }) {
                    Text(
                        text = selectedSeason?.let { "Season $it" } ?: "Select a Season",
                        color = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    seasons.forEach { season ->
                        DropdownMenuItem(
                            text = {
                                Text(text = "Season ${season.season}")
                            },
                            onClick = {
                                selectedSeason = season.season
                                expanded = false
                                onSeasonSelected(season.season)  // Notify parent to fetch episodes
                            }
                        )
                    }
                }
            }

            // Display Episodes if a season is selected
            LazyColumn(modifier = Modifier.padding(0.dp)) {
                items(episodes) { episode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() // Make each episode take the full width
                            .padding(8.dp)
                            .clickable {
                                val intent = Intent(context, WatchPage::class.java).apply {
                                    putExtra("season", episode.season)
                                    putExtra("episode", episode.episode)
                                    putExtra("entityId", entityDetail.id)
                                    putExtra("userId", userId)
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        // Display miniatura or fallback to wall image
                        val imageUrl = if (episode.miniatura.isNullOrEmpty()) {
                            entityDetail.wall
                        } else {
                            episode.miniatura
                        }

                        // Image of the episode
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Episode details: Season/Episode, title, and description
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                        ) {
                            Text(
                                text = "S${episode.season}E${episode.episode}: ${episode.title}",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = episode.description,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Add space between episodes
                }
            }
        }
    }
    }






