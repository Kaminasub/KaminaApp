package com.kamina.app

import Navbar
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamina.app.api.EntityResponse
import com.kamina.app.api.fetchEntities
import com.kamina.app.ui.theme.GradientBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Color
import com.kamina.app.ui.theme.KaminaAppTheme

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        setContent {
            KaminaAppTheme {
                GradientBackground {  // Use the GradientBackground composable directly
                    if (userId != null) {
                        HomePage() // Display homepage content
                    } else {
                        Text("Error: User not logged in")
                    }
                    }
                }
            }
        }

        @Composable
        fun HomePage() {
            var entities by remember { mutableStateOf<List<EntityResponse>?>(null) }
            val listState = rememberLazyListState()
            var currentIndex by remember { mutableStateOf(0) }
            var isUserInteracting by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                fetchEntities { response ->
                    entities = response
                }
            }

            LaunchedEffect(entities) {
                if (entities != null && entities!!.isNotEmpty()) {
                    coroutineScope.launch {
                        while (true) {
                            delay(7000L)
                            if (!isUserInteracting) {
                                currentIndex = (currentIndex + 1) % entities!!.size
                                listState.scrollToItem(currentIndex)
                            }
                        }
                    }
                }
            }

            LaunchedEffect(listState.isScrollInProgress) {
                if (listState.isScrollInProgress) {
                    isUserInteracting = true
                } else {
                    coroutineScope.launch {
                        delay(5000L)
                        isUserInteracting = false
                    }
                    currentIndex = listState.firstVisibleItemIndex
                    listState.scrollToItem(currentIndex)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.Top
            ) {
                Navbar()  // Check if Navbar itself has padding inside

                Spacer(modifier = Modifier.height(0.dp))

                // Carousel content
                entities?.let {
                    LazyRow(
                        state = listState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(it) { index, entity ->
                            val nextEntity = if (index + 1 < it.size) it[index + 1] else null
                            CarouselItem(entity = entity, nextEntity = nextEntity)
                        }
                    }
                } ?: run {
                    Text("Loading...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }