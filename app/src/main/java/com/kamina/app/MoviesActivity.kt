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
    fun MoviesPageScreen(userId: Int) {  // Change userId type to Int
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

                    // MoviesPage composable with movies content and pass userId
                    MoviesPage(userId = userId)  // Pass userId to MoviesPage
                }
            }
        )
    }
}
