package com.kamina.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kamina.app.ui.theme.KaminaAppTheme

class TvPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve userId and any other relevant extras from the intent
        val userId = intent.getIntExtra("userId", -1)

        // Set up the content view with Compose
        setContent {
            // Apply the app theme
            KaminaAppTheme {
                // Load TvPage with the required parameters
                TvPage(userId = userId)
            }
        }
    }
}
