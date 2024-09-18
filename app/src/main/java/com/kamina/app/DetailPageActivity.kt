package com.kamina.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.kamina.app.ui.theme.KaminaAppTheme


class DetailPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve entityId from intent
        val entityId = intent?.getIntExtra("entityId", 0) ?: 0
        Log.d("DetailPageActivity", "Received entityId: $entityId")

        if (entityId == 0) {
            Log.e("DetailPageActivity", "Invalid entityId, stopping activity.")
            setContent {
                KaminaAppTheme {
                    Text(text = "Invalid entity ID", color = androidx.compose.ui.graphics.Color.Red)
                }
            }
        } else {
            // Load detail page content
            setContent {
                KaminaAppTheme {
                    DetailPageScreen(entityId = entityId)
                }
            }
        }
    }
}
