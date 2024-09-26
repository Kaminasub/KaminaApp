
import android.app.Activity
import android.content.Context
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.ConfigurationActivity
import com.kamina.app.HomePageActivity
import com.kamina.app.LoginActivity
import com.kamina.app.MoviesActivity
import com.kamina.app.R
import com.kamina.app.SearchPageActivity
import com.kamina.app.SeriesActivity
import com.kamina.app.api.ApiService
import com.kamina.app.api.UserApiHelper
import com.kamina.app.api.UserIconResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun Navbar(navController: NavHostController, avatarChanged: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Retrieve the userId from SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getString("userId", null)

    // Fetch user icon if userId is not null
    var userIconUrl by remember { mutableStateOf<String?>(null) }

    // Re-fetch user icon when avatarChanged is toggled
    LaunchedEffect(userId, avatarChanged) {
        userId?.let {
            UserApiHelper.fetchUserIcon(it) { iconUrl ->
                userIconUrl = iconUrl
            }
        }
    }

    val painter = rememberAsyncImagePainter(userIconUrl ?: "")

    // Gradient background colors
    val gradientColors = listOf(
        Color(0xFF9B34EF),  // #9b34ef
        Color(0xFF490CB0),  // #490cb0
        Color.Transparent   // Tertiary color
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(colors = gradientColors))  // Apply gradient background
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Skull Icon (Navigate to Home)
        Image(
            painter = painterResource(id = R.drawable.skull),
            contentDescription = "Home",
            modifier = Modifier
                .size(50.dp)
                .clickable {
                    // Direct Intent to HomePageActivity (Activity navigation)
                    val intent = Intent(context, HomePageActivity::class.java)
                    context.startActivity(intent)
                }
        )

        // Right User Icon with Dropdown Menu
        Box {
            Image(
                painter = painter, // Coil painter to load user icon
                contentDescription = "User Menu",
                modifier = Modifier
                    .size(50.dp)
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(120.dp)
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Direct Intent to HomePageActivity
                        val intent = Intent(context, HomePageActivity::class.java)
                        context.startActivity(intent)
                    },
                    text = { Text("Home") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        val intent = Intent(context, SeriesActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finishAffinity()
                    },
                    text = { Text("Series") }
                )

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        val intent = Intent(context, MoviesActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finishAffinity()
                    },
                    text = { Text("Movies") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        val intent = Intent(context, SearchPageActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finishAffinity()
                    },
                    text = { Text("Search") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        val intent = Intent(context, ConfigurationActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finishAffinity()
                    },
                    text = { Text("Configuration") }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // Direct Intent to LoginActivity (for logout)
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
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
                val iconUrl = "https://api.kaminajp.com" + response.body()?.userIcon
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
