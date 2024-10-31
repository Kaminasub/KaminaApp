package com.kamina.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.kamina.app.ui.theme.KaminaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable immersive full-screen mode using WindowInsetsController
        setFullScreenMode()

        setContent {
            KaminaAppTheme {
                val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString("userId", null) ?: ""
                val userLanguage = sharedPreferences.getString("appLanguage", "en") ?: "en" // Ensure not null

                // Display the HomePage if the user is logged in, otherwise display an error message
                if (userId.isNotEmpty()) {
                    // Convert userId to Int and pass to HomePage
                    HomePage(userId = userId.toInt(), userLanguage = userLanguage)
                } else {
                    Text("Error: User not logged in", color = Color.White)
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
            controller?.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
