package com.kamina.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kamina.app.ui.theme.KaminaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaminaAppTheme {
                val navController = rememberNavController()
                val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString("userId", null) ?: ""

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        val userId = "userIdPlaceholder"
                        HomePage(userId = userId, navController = navController)
                    }
                    composable("series") { SeriesPage() }
                    composable("movies") { MoviesPage() }
                    composable("search") { SearchPage(navController = navController) }
                    composable("configuration") {
                        ConfigurationPage(userId = userId, setUserIcon = {})
                    }
                    //composable("login") { LoginPage() }
                    // Add detail page composable with a parameter for entityId
                    composable("detailpage/{entityId}") { backStackEntry ->
                        val entityId = backStackEntry.arguments?.getString("entityId")?.toIntOrNull() ?: 0
                        DetailPageScreen(entityId = entityId)
                    }
                }
            }
        }
    }
}
