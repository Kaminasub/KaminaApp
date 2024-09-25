package com.kamina.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.kamina.app.ui.theme.KaminaAppTheme

class SearchPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)?.toIntOrNull() ?: 0  // Convert to Int, default to 0 if null

        setContent {
            KaminaAppTheme {
                val navController = rememberNavController() // Create NavHostController
                SearchPage(navController = navController, userId = userId) // Pass NavHostController and userId to SearchPage
            }
        }
    }
}


