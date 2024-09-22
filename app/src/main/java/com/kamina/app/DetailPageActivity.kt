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

        // Retrieve entityId and userId from intent
        val entityId = intent?.getIntExtra("entityId", 0) ?: 0
        val userId = intent?.getIntExtra("userId", 0) ?: 0 // Assuming userId is passed as well
        Log.d("DetailPageActivity", "Received entityId: $entityId, userId: $userId")

        if (entityId == 0) {
            Log.e("DetailPageActivity", "Invalid entityId, stopping activity.")
            setContent {
                KaminaAppTheme {
                    Text(text = "Invalid entity ID", color = androidx.compose.ui.graphics.Color.Red)
                }
            }
        } else {
            // Load detail page content and pass the entityId and userId to DetailPageScreen
            setContent {
                KaminaAppTheme {
                    DetailPageScreen(entityId = entityId, userId = userId)  // Pass both entityId and userId to the composable
                }
            }
        }
    }
}
