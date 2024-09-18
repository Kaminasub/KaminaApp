package com.kamina.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamina.app.api.AvatarManager
import com.kamina.app.api.changeUserPin

@Composable
fun ProfileConfigurationPage(
    userId: String,
    setUserIcon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var avatars by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var successMessage by remember { mutableStateOf<String?>(null) } // Initialize successMessage state
    var avatarChanged by remember { mutableStateOf(false) } // Track avatar change for refresh

    // Fetch avatars dynamically from the API
    LaunchedEffect(Unit) {
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

    // Handle page refresh after avatar change
    LaunchedEffect(avatarChanged) {
        if (avatarChanged) {
            avatarChanged = false
        }
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

    Column(modifier = modifier.padding(16.dp)) {
        // Show the success message if available
        successMessage?.let {
            Text(text = it, color = androidx.compose.ui.graphics.Color.Green)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // PIN change form UI here
        TextField(
            value = oldPin,
            onValueChange = { oldPin = it },
            label = { Text("Old PIN") }
        )

        TextField(
            value = newPin,
            onValueChange = { newPin = it },
            label = { Text("New PIN") }
        )

        TextField(
            value = confirmNewPin,
            onValueChange = { confirmNewPin = it },
            label = { Text("Confirm New PIN") }
        )

        // Button to trigger PIN change
        Button(onClick = { handlePinChange() }) {
            Text("Change PIN")
        }
    }
}