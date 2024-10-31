package com.kamina.app


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.AvatarManager
import com.kamina.app.api.changeUserPin


@Composable
fun ConfigurationPage(
    userId: String,
    setUserIcon: (String) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
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
        loading = true
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
                successMessage = if (success) {
                    "PIN changed successfully"
                } else {
                    "Failed to change PIN"
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
                        Color(0xFF182459),
                        Color(0xFF14143C),
                        Color(0xFF000000)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(x = 1000f, y = -1000f)
                )
            )
    ) {
        // Display the success message if available
        successMessage?.let {
            Text(text = it, color = Color.Green)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Avatar Selection
        if (loading) {
            Text(text = "Loading avatars...")
        } else {
            Text(text = "Select Your Avatar")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(avatars) { avatarUrl ->
                    var isFocused by remember { mutableStateOf(false) }

                    Image(
                        painter = rememberAsyncImagePainter(avatarUrl),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused  // Update the state when focus changes
                            }
                            .focusable()
                            .then(
                                if (isFocused) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable {
                                handleAvatarChange(avatarUrl)  // Handle avatar selection and refresh
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Language Selection Section
        Text(text = "Select Language", fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("en" to "English", "es" to "Español", "cz" to "Czech").forEach { (code, name) ->
                Button(
                    onClick = { onLanguageChange(code) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLanguage == code) Color.Green else Color.Gray
                    )
                ) {
                    Text(text = name, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PIN Change Form
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Change PIN")

            // Old PIN
            TextField(
                value = oldPin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) oldPin = newValue
                },
                label = { Text("Old PIN") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            // New PIN
            TextField(
                value = newPin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) newPin = newValue
                },
                label = { Text("New PIN") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            // Confirm New PIN
            TextField(
                value = confirmNewPin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) confirmNewPin = newValue
                },
                label = { Text("Confirm New PIN") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            // Change PIN Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { handlePinChange() }) {
                Text("Change PIN")
            }
        }
    }
}
