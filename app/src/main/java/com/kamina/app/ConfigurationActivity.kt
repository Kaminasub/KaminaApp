package com.kamina.app

import Navbar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.AvatarManager
import com.kamina.app.api.changeUserPin
import com.kamina.app.ui.theme.KaminaAppTheme

class ConfigurationActivity : ComponentActivity() {

    private val avatarManager = AvatarManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null) ?: ""

        setContent {
            KaminaAppTheme {
                val navController = rememberNavController()

                // Add a state to track avatar changes
                var avatarChanged by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        Navbar(navController = navController, avatarChanged)  // Pass avatarChanged state
                    },
                    content = { paddingValues ->
                        ConfigurationPage(
                            userId = userId,
                            setUserIcon = { avatarPath ->
                                updateUserAvatar(userId, avatarPath) {
                                    avatarChanged = !avatarChanged  // Toggle state to trigger refresh
                                }
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                )
            }
        }
    }

    // Change this function to update avatar with the avatar URL and trigger refresh
    private fun updateUserAvatar(userId: String, avatarUrl: String, onSuccess: () -> Unit) {
        avatarManager.updateUserAvatar(userId.toInt(), avatarUrl, onSuccess = {
            // Handle avatar update success and trigger the callback
            onSuccess()
        }, onFailure = {
            // Handle avatar update failure, show error message if necessary
        })
    }
}

@Composable
fun ConfigurationPage(
    userId: String,
    setUserIcon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var avatars by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var avatarChanged by remember { mutableStateOf(false) }

    // Fetch avatars when the composable is launched or when avatarChanged is updated
    LaunchedEffect(avatarChanged) {
        loading = true // Set loading to true again before fetching avatars
        AvatarManager().fetchAvatars(
            onSuccess = { fetchedAvatars ->
                avatars = fetchedAvatars.map { "https://api.kaminajp.com$it" }
                loading = false
            },
            onFailure = {
                loading = false
            }
        )
    }

    // Handle PIN change logic
    fun handlePinChange() {
        if (newPin == confirmNewPin) {
            changeUserPin(userId, oldPin, newPin) { success ->
                if (success) {
                    successMessage = "PIN changed successfully"
                } else {
                    successMessage = "Failed to change PIN"
                }
            }
        } else {
            successMessage = "New PIN and confirmation do not match"
        }
    }

    // Handle Avatar Change and Force Refresh
    fun handleAvatarChange(avatarUrl: String) {
        setUserIcon(avatarUrl)
        avatarChanged = !avatarChanged // Toggle the state to trigger a re-fetch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF9B34EF),  // #9b34ef
                        Color(0xFF490CB0),  // #490cb0
                        Color.Transparent   // Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)  // Adjust this to control the gradient angle
                )
            )
    ) {
        // Avatar Selection
        if (loading) {
            Text(text = "Loading avatars...")
        } else {
            Text(text = "Select Your Avatar")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(avatars) { avatarUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(avatarUrl),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                handleAvatarChange(avatarUrl)  // Handle avatar selection and refresh
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIN Change Form
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Change PIN")

            // Show the success message if available
            successMessage?.let {
                Text(text = it, color = androidx.compose.ui.graphics.Color.Green)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Old PIN
            Text("Old PIN")
            TextField(
                value = oldPin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) oldPin = newValue
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                visualTransformation = PasswordVisualTransformation() // Hides the PIN input for security
            )

            // New PIN
            Text("New PIN")
            TextField(
                value = newPin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) newPin = newValue
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                visualTransformation = PasswordVisualTransformation()
            )

            // Confirm New PIN
            Text("Confirm New PIN")
            TextField(
                value = confirmNewPin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) confirmNewPin = newValue
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                visualTransformation = PasswordVisualTransformation()
            )

            // Change PIN Button
            Button(onClick = { handlePinChange() }) {
                Text("Change PIN")
            }
        }
    }
}
