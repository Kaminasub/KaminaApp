package com.kamina.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.kamina.app.ui.theme.KaminaAppTheme

class SearchPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaminaAppTheme {
                val navController = rememberNavController() // Create NavHostController
                SearchPage(navController = navController) // Pass NavHostController to SearchPage
            }
        }
    }
}
