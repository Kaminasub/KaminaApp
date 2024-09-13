package com.kamina.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top // Align the navbar at the top
    ) {
        // Your other content goes below the Navbar
        Text("Home Page", modifier = Modifier.align(Alignment.CenterHorizontally))

        // Pass the userId to the Navbar
        Navbar(userId = userId)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KaminaAppTheme {
        HomePage(userId = "1") // Provide a dummy userId for the preview
    }
}
