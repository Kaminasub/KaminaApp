package com.kamina.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kamina.app.ui.EmbeddedVideoPage
import com.kamina.app.ui.theme.KaminaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreenMode() // Keep immersive mode as you already have

        setContent {
            KaminaAppTheme {
                val navController = rememberNavController()
                val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString("userId", null) ?: ""

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomePage(userId = userId, navController = navController)
                    }
                    composable("series") { SeriesPage() }
                    composable("movies") { MoviesPage() }
                    composable("search") { SearchPage(navController = navController) }
                    composable("configuration") {
                        ConfigurationPage(userId = userId, setUserIcon = {})
                    }
                    composable("detailpage/{entityId}/{userId}") { backStackEntry ->
                        val entityId = backStackEntry.arguments?.getString("entityId")?.toIntOrNull() ?: 0
                        val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        DetailPageScreen(entityId = entityId, userId = userId)
                    }
                    // New composable for embedding video
                    composable("embedVideoPage") {
                        EmbeddedVideoPage() // Load the video in this screen
                    }
                }
            }
        }
    }

    private fun setFullScreenMode() {
        // Keeping your full-screen implementation as is
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
