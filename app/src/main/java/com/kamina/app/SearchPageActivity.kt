package com.kamina.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.kamina.app.ui.theme.KaminaAppTheme

class SearchPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId and language from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)?.toIntOrNull() ?: 0  // Convert to Int, default to 0 if null
        val userLanguage = sharedPreferences.getString("appLanguage", "en") ?: "en"  // Fetch user's language or default to "en"

        setContent {
            KaminaAppTheme {
                // Add a condition to check if userId is valid
                if (userId != 0) {
                    // Pass both userId and userLanguage to SearchPage composable
                    SearchPage(userId = userId, userLanguage = userLanguage)
                } else {
                    // Show an error message if the user is not logged in
                    Text(
                        text = "Error: User not logged in",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
