package com.kamina.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.UserApiHelper
import com.kamina.app.ui.components.UserDropdownMenu
import com.kamina.app.ui.theme.KaminaAppTheme

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId and userLanguage from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null) // Get userId without default value
        val userLanguage = sharedPreferences.getString("appLanguage", null) // Get userLanguage without default value

        setContent {
            KaminaAppTheme {
                if (userId != null && userLanguage != null) {
                    // If userId and userLanguage exist, proceed to load HomePage
                    HomePage(userId = userId.toInt(), userLanguage = userLanguage)
                } else {
                    // Show error message if session data is missing
                    Text("Error: User not logged in", color = Color.White)
                    LaunchedEffect(Unit) {
                        Toast.makeText(this@HomePageActivity, "Session expired, please log in again.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@HomePageActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // Close HomePageActivity
                    }
                }
            }
        }
    }
}

@Composable
fun HomePage(userId: Int, userLanguage: String) {
    val context = LocalContext.current
    var userIconUrl by remember { mutableStateOf<String?>(null) }

    // Fetch user icon based on userId
    LaunchedEffect(userId) {
        UserApiHelper.fetchUserIcon(userId.toString()) { iconUrl ->
            userIconUrl = iconUrl
        }
    }

    val painter = rememberAsyncImagePainter(userIconUrl ?: "")
    var isFocused by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF182459), // Gradient colors
                                Color(0xFF14143C),
                                Color(0xFF000000)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(x = 1000f, y = -1000f)
                        )
                    )
            ) {
                // User icon and dropdown menu
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.dp, end = 10.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            }
                            .focusable()
                            .then(
                                if (isFocused) {
                                    Modifier
                                        .border(
                                            width = 3.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .padding(3.dp)
                                } else {
                                    Modifier
                                }
                            )
                            .clip(CircleShape)
                            .clickable {
                                showDropdownMenu = true
                            }
                    )
                    UserDropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        context = context
                    )
                }

                // Only include CombinedSections here (categories and continue watching)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = 55.dp)
                ) {
                    // Include the combined sections which will now include the carousel
                    CombinedSections(userId = userId, userLanguage = userLanguage)
                }
            }
        }
    )
}
