package com.kamina.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.UserApiHelper
import com.kamina.app.ui.components.UserDropdownMenu
import com.kamina.app.ui.theme.GradientBackground
import com.kamina.app.ui.theme.KaminaAppTheme


class MoviesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)?.toIntOrNull() ?: 0  // Convert to Int

        // Fetch the user's preferred language from SharedPreferences
        val userLanguage = sharedPreferences.getString("appLanguage", "en") ?: "en"  // Default to 'en'

        setContent {
            KaminaAppTheme {
                GradientBackground {
                    if (userId != 0) {
                        MoviesPageScreen(userId = userId, userLanguage = userLanguage)  // Pass the userId and userLanguage to MoviesPage composable
                    } else {
                        Text(
                            text = "Error: User not logged in",
                            color = Color.White,
                            fontSize = 24.sp,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge // or another appropriate style
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MoviesPageScreen(userId: Int, userLanguage: String) {
        val context = LocalContext.current
        var userIconUrl by remember { mutableStateOf<String?>(null) }

        // Fetch user icon if userId is not null
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
                                    Color(0xFF182459), // #663dff at 0%
                                    Color(0xFF14143C),
                                    Color(0xFF000000)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(x = 1000f, y = -1000f)  // Adjust this to control the gradient angle
                            )
                        )
                ) {
                    // Box to hold both User Icon and DropdownMenu for alignment
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 5.dp, end = 10.dp) // icon padding
                    ) {
                        // User Icon in the top right corner
                        Image(
                            painter = painter,
                            contentDescription = "User Icon",
                            modifier = Modifier
                                .size(40.dp)  //icon size
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused  // Update the state when focus changes
                                }
                                .focusable()  // Make the icon focusable
                                .then(
                                    if (isFocused) {
                                        Modifier
                                            .border(
                                                width = 3.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .padding(3.dp)  // Optional: to prevent the border from overlapping with the icon
                                    } else {
                                        Modifier
                                    }
                                )
                                .clip(CircleShape)  // Make sure the icon and selector are circular
                                .clickable {
                                    showDropdownMenu = true // Show the dropdown menu when clicked
                                }
                        )

                        // Align DropdownMenu under User Icon
                        UserDropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false },
                            context = context
                        )
                    }

                    // MoviesPage composable with movies content, pass userId and userLanguage
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(top = 55.dp) // Adjust padding to avoid overlap with the user icon
                    ) {
                        Spacer(modifier = Modifier.height(0.dp)) //spacio entre icon y thumbnails

                        MoviesPage(userId = userId, userLanguage = userLanguage)
                    }
                }
            }
        )
    }
}
