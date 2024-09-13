package com.kamina.app

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.ApiService
import com.kamina.app.api.UserIconResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun Navbar(navController: NavHostController = rememberNavController(), userId: String) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Fetch user icon from API using the user ID
    var userIconUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        fetchUserIcon(userId) { iconUrl ->
            userIconUrl = iconUrl
        }
    }

    val painter = rememberAsyncImagePainter(userIconUrl ?: "") // Use Coil to load the icon

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Skull Icon (Navigate to Home)
        Image(
            painter = painterResource(id = R.drawable.skull), // Skull icon from drawable
            contentDescription = "Home",
            modifier = Modifier
                .size(50.dp)
                .clickable {
                    navController.navigate("home")
                }
        )

        // Right User Icon with Dropdown Menu
        Box {
            Image(
                painter = painter, // Coil painter to load user icon
                contentDescription = "User Menu",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
                modifier = Modifier
                    .width(150.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Handle logout
                        val intent = Intent(context, HomePageActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finishAffinity()
                    },
                    text = { Text("home") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Navigate to Series
                        navController.navigate("series")
                    },
                    text = { Text("Series") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Navigate to Movies
                        navController.navigate("movies")
                    },
                    text = { Text("Movies") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Navigate to Search
                        navController.navigate("search")
                    },
                    text = { Text("Search") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Navigate to Configuration
                        navController.navigate("configuration")
                    },
                    text = { Text("Configuration") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Handle logout
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finishAffinity()
                    },
                    text = { Text("Logout") }
                )
            }
        }
    }
}

// Function to fetch the user icon from the backend
fun fetchUserIcon(userId: String, onResult: (String?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(ApiService::class.java)

    api.getUserIcon(userId).enqueue(object : Callback<UserIconResponse> {
        override fun onResponse(call: Call<UserIconResponse>, response: Response<UserIconResponse>) {
            if (response.isSuccessful) {
                val iconUrl = response.body()?.userIcon
                onResult(iconUrl)
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: Call<UserIconResponse>, t: Throwable) {
            onResult(null)
        }
    })
}