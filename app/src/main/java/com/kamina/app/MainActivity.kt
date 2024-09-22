package com.kamina.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kamina.app.ui.theme.KaminaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable immersive full-screen mode using WindowInsetsController
        setFullScreenMode()

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
                    composable("detailpage/{entityId}/{userId}") { backStackEntry ->
                        val entityId = backStackEntry.arguments?.getString("entityId")?.toIntOrNull() ?: 0
                        val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0

                        DetailPageScreen(entityId = entityId, userId = userId)
                    }
                }
            }
        }
    }

    // Reapply immersive mode when the app regains focus
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setFullScreenMode()
        }
    }

    private fun setFullScreenMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}
