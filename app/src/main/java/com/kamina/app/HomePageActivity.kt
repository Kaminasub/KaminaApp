package com.kamina.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.EntityResponse
import com.kamina.app.api.fetchEntities
import com.kamina.app.ui.theme.KaminaAppTheme


class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get userId, you can retrieve it from intent extras, shared preferences, or any data source
        val userId = "1"  // Replace this with the actual logic to fetch the user ID

        setContent {
            KaminaAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomePage(userId)
                }
            }
        }
    }
}

@Composable
fun HomePage(userId: String) {
    var entities by remember { mutableStateOf<List<EntityResponse>?>(null) }

    LaunchedEffect(Unit) {
        fetchEntities { response ->
            entities = response
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Navbar(userId = userId)

        Spacer(modifier = Modifier.height(16.dp))
// Carousel
        Text(
            text = "",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black, // explicitly specify the color
            fontSize = 20.sp // explicitly specify the font size
        )


        entities?.let {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                items(it) { entity ->
                    CarouselItem(entity = entity)
                }
            }
        } ?: run {
            Text("Loading...", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun CarouselItem(entity: EntityResponse) {
    Box(
        modifier = Modifier
            .width(400.dp)
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(entity.pic),
            contentDescription = "Entity Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        if (entity.logo != null) {
            Image(
                painter = rememberAsyncImagePainter(entity.logo),
                contentDescription = "Entity Logo",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(150.dp)
            )
        } else {
            Text(
                text = entity.name,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KaminaAppTheme {
        HomePage(userId = "27")
    }
}