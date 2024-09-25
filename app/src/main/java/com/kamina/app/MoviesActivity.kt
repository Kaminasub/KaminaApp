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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.kamina.app.ui.theme.GradientBackground
import com.kamina.app.ui.theme.KaminaAppTheme
import androidx.compose.foundation.layout.Box




class MoviesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId from SharedPreferences and convert it to Int
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)?.toIntOrNull() ?: 0  // Convert userId to Int

        setContent {
            KaminaAppTheme {
                GradientBackground {
                    if (userId != 0) {  // Check if userId is valid
                        MoviesPageScreen(userId)  // Pass the userId to the MoviesPageScreen composable
                    } else {
                        Text("Error: User not logged in")
                    }
                }
            }
        }
    }

    @Composable
    fun MoviesPageScreen(userId: Int) {
        val navController = rememberNavController()

        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)  // Transparent Navbar
                ) {
                    Navbar(navController = navController, avatarChanged = false)
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF9B34EF),  // #9b34ef
                                    Color(0xFF490CB0),  // #490cb0
                                    Color.Transparent   // Transparent
                                ),
                                start = Offset(0f, 0f),  // Start of the gradient
                                end = Offset(1000f, 1000f)  // End of the gradient (adjust this to control the angle)
                            )
                        ),
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // MoviesPage composable with movies content
                    MoviesPage(userId = userId)  // Pass userId to MoviesPage
                }
            }
        )
    }
}

