package com.kamina.app

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.AvatarManager
import com.kamina.app.api.UserApiHelper
import com.kamina.app.api.fetchUserLanguage
import com.kamina.app.api.updateUserLanguage
import com.kamina.app.ui.components.UserDropdownMenu
import com.kamina.app.ui.theme.KaminaAppTheme


class ConfigurationActivity : ComponentActivity() {

    private val avatarManager = AvatarManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null) ?: ""

        // Initially, set a loading state for the language until the API response is received
        var defaultLanguage = "en" // Default to English

        setContent {
            KaminaAppTheme {
                // Add states to track avatar changes and language selection
                var avatarChanged by remember { mutableStateOf(false) }
                var selectedLanguage by remember { mutableStateOf<String?>(null) } // Start with null until API fetches it
                var isLoading by remember { mutableStateOf(true) } // Loading state
                var showDropdownMenu by remember { mutableStateOf(false) } // To control the dropdown menu visibility
                var userIconUrl by remember { mutableStateOf<String?>(null) } // Track the user icon URL

                // Fetch the user's language from the backend and update the language state
                LaunchedEffect(userId) {
                    fetchUserLanguage(userId) { languageFromApi ->
                        selectedLanguage = languageFromApi ?: defaultLanguage
                        isLoading = false // Disable the loading state after fetching the language
                    }

                    // Fetch the user icon initially
                    UserApiHelper.fetchUserIcon(userId) { iconUrl ->
                        userIconUrl = iconUrl
                    }
                }

                if (isLoading) {
                    // Show a loading screen or message while the language is being fetched
                    Text(text = "Loading...", color = Color.White)
                } else {
                    Scaffold(
                        content = { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF182459),
                                                Color(0xFF14143C),
                                                Color(0xFF000000)
                                            ),
                                            start = Offset(0f, 0f),
                                            end = Offset(x = 1000f, y = -1000f)
                                        )
                                    )
                            ) {
                                // Box to hold both User Icon and DropdownMenu for alignment
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 5.dp, end = 10.dp)
                                ) {
                                    val painter = rememberAsyncImagePainter(userIconUrl ?: "")
                                    var isFocused by remember { mutableStateOf(false) }
                                    var showDropdownMenu by remember { mutableStateOf(false) }

                                    // User Icon in the top right corner
                                    Image(
                                        painter = painter,
                                        contentDescription = "User Icon",
                                        modifier = Modifier
                                            .size(40.dp)
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
                                        context = this@ConfigurationActivity
                                    )
                                }

                                // Configuration Page Content
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 55.dp)
                                ) {
                                    ConfigurationPage(
                                        userId = userId,
                                        setUserIcon = { avatarPath ->
                                            updateUserAvatar(userId, avatarPath) {
                                                avatarChanged = !avatarChanged
                                                // Fetch the updated user icon after changing the avatar
                                                UserApiHelper.fetchUserIcon(userId) { updatedIconUrl ->
                                                    userIconUrl = updatedIconUrl // Update the icon URL
                                                }
                                            }
                                        },
                                        selectedLanguage = selectedLanguage!!,
                                        onLanguageChange = { newLanguage ->
                                            selectedLanguage = newLanguage
                                            // Update the language in the backend and shared preferences
                                            updateUserLanguage(userId, newLanguage) { success ->
                                                if (success) {
                                                    val editor = sharedPreferences.edit()
                                                    editor.putString("appLanguage", newLanguage)
                                                    editor.apply()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun updateUserAvatar(userId: String, avatarUrl: String, onSuccess: () -> Unit) {
        avatarManager.updateUserAvatar(userId.toInt(), avatarUrl, onSuccess = {
            onSuccess()
        }, onFailure = {
            // Handle avatar update failure
        })
    }
}


