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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.kamina.app.ui.theme.GradientBackground
import com.kamina.app.ui.theme.KaminaAppTheme


class SeriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        setContent {
            KaminaAppTheme {
                GradientBackground {
                    if (userId != null) {
                        SeriesPageScreen(userId)  // Pass the userId to the SeriesPage composable
                    } else {
                        Text("Error: User not logged in")
                    }
                }
            }
        }
    }


    @Composable
    fun SeriesPageScreen(userId: String) {
        val navController = rememberNavController() // Create a dummy NavController

        Scaffold(
            topBar = { Navbar(navController = navController, avatarChanged = false) },  // Pass NavController and avatarChanged
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(2.dp)
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // SeriesPage composable with series content
                    SeriesPage()  // Call the SeriesPage composable here
                }
            }
        )
    }
}
