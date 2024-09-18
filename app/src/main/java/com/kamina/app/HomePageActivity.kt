package com.kamina.app

import Navbar
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kamina.app.api.EntityResponse
import com.kamina.app.api.fetchEntities
import com.kamina.app.ui.theme.KaminaAppTheme

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        setContent {
            KaminaAppTheme {
                if (userId != null) {
                    val navController = rememberNavController()
                    HomePage(userId = userId, navController = navController)
                } else {
                    Text("Error: User not logged in")
                }
            }
        }
    }
}

@Composable
fun HomePage(userId: String, navController: NavHostController) {
    var entities by remember { mutableStateOf<List<EntityResponse>?>(null) }

    // Fetch entities for the carousel
    LaunchedEffect(Unit) {
        fetchEntities { response ->
            entities = response
        }
    }

    // Scaffold with top Navbar
    Scaffold(
        topBar = { Navbar(navController = navController, avatarChanged = false) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)  // Apply paddingValues here
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Pass the entities to CombinedSections
                entities?.let {
                    CombinedSections(userId = userId, entities = it)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    )

}

