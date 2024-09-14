package com.kamina.app

import Navbar
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamina.app.api.EntityResponse
import com.kamina.app.api.fetchEntities
import com.kamina.app.ui.theme.KaminaAppTheme
import androidx.compose.foundation.layout.fillMaxSize

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        setContent {
            KaminaAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (userId != null) {
                        HomePage() // No need to pass userId here
                    } else {
                        Text("Error: User not logged in")
                    }
                }
            }
        }
    }
}

@Composable
fun HomePage() {
    var entities by remember { mutableStateOf<List<EntityResponse>?>(null) }

    LaunchedEffect(Unit) {
        fetchEntities { response ->
            entities = response
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Navbar()

        Spacer(modifier = Modifier.height(16.dp))

        // Carousel
        Text(
            text = "",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black,
            fontSize = 20.sp
        )

        entities?.let {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                itemsIndexed(it) { index, entity ->
                    // Get the next entity for pre-fetching
                    val nextEntity = if (index + 1 < it.size) it[index + 1] else null
                    CarouselItem(entity = entity, nextEntity = nextEntity)
                }
            }
        } ?: run {
            Text("Loading...", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

suspend fun loadMoreEntities(
    currentEntities: List<EntityResponse>,
    isLoading: MutableState<Boolean>,  // Expect MutableState<Boolean>
    onEntitiesLoaded: (List<EntityResponse>) -> Unit
) {
    isLoading.value = true

    // Simulate network delay (replace this with actual API call in a real app)
    kotlinx.coroutines.delay(1000)

    fetchEntities { newEntities ->
        isLoading.value = false
        if (!newEntities.isNullOrEmpty()) {
            onEntitiesLoaded(newEntities)
        }
    }
}

