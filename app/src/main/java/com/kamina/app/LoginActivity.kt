package com.kamina.app



import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.util.Log
import com.kamina.app.api.ApiService
import com.kamina.app.api.LoginResponse
import com.kamina.app.api.PinLoginRequest
import com.kamina.app.api.PinlessLoginRequest
import com.kamina.app.api.UsernameResponse
import com.kamina.app.ui.components.CustomButton
import com.kamina.app.ui.theme.KaminaAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KaminaAppTheme {
                LoginScreen()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun LoginScreen() {
        var selectedUsername by remember { mutableStateOf("Select Username") }
        var selectedLanguage by remember { mutableStateOf("en") } // Default language
        var pin by remember { mutableStateOf("") }
        var isPinInputVisible by remember { mutableStateOf(false) }
        var users by remember { mutableStateOf(listOf<Triple<String, String, String>>()) } // Triple (username, userIcon, language)
        var toastMessage by remember { mutableStateOf<String?>(null) }

        val context = LocalContext.current

        // Fetch usernames and userIcons from the API on screen load
        LaunchedEffect(Unit) {
            fetchUsers { fetchedUsers ->
                users = fetchedUsers
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the user icons with usernames underneath
            if (users.isNotEmpty() && !isPinInputVisible) {
                Text(
                    "Select a User",
                    fontSize = 25.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Use LazyVerticalGrid to display users in a two-column grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Two columns
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Space between rows
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between columns
                ) {
                    items(users.size) { index ->
                        val (username, userIcon, language) = users[index]

                        // Local state to track focus for each item
                        var isFocused by remember { mutableStateOf(false) }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally, // Center the content horizontally
                            verticalArrangement = Arrangement.Center // Center vertically within the column
                        ) {
                            // User Icon (clickable) with focus handling for the white outline
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .onFocusChanged { focusState ->
                                        isFocused = focusState.isFocused
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
                                            Modifier.border(
                                                width = 0.dp,
                                                color = Color.Transparent,
                                                shape = CircleShape
                                            )
                                        }
                                    )
                                    .clickable {
                                        selectedUsername = username
                                        selectedLanguage = language  // Set the selected language

                                        // If the username is "mama" or "darina", skip PIN input
                                        if (username == "mama" || username == "darina") {
                                            performPinlessLogin(selectedUsername, selectedLanguage, context) { message ->
                                                toastMessage = message
                                            }
                                        } else {
                                            isPinInputVisible = true
                                        }
                                        Log.d("LoginActivity", "Selected language: $language")  // Log the selected language
                                    }
                            ) {
                                Image(
                                    painter = rememberImagePainter(data = userIcon, builder = {
                                        crossfade(true)
                                    }),
                                    contentDescription = "User Icon",
                                    modifier = Modifier.size(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Username below the icon
                            Text(
                                text = username,
                                color = if (selectedUsername == username) Color.Black else Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Show the PIN Input field, login button, and selected user icon only after a user is selected
            if (isPinInputVisible) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    users.firstOrNull { it.first == selectedUsername }?.let { (_, userIcon, _) ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 3.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                        ) {
                            Image(
                                painter = rememberImagePainter(data = userIcon, builder = {
                                    crossfade(true)
                                }),
                                contentDescription = "Selected User Icon",
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text(
                        text = selectedUsername,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 4) {
                                pin = it
                            }
                        },
                        label = { Text("Enter PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(120.dp)
                            .defaultMinSize(minHeight = 56.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButton(
                        text = "Login",
                        onClick = {
                            if (pin.isNotEmpty()) {
                                performPinLogin(selectedUsername, pin, selectedLanguage, context) { message ->
                                    toastMessage = message
                                }
                            } else {
                                toastMessage = "PIN cannot be empty"
                            }
                        },
                        textSize = 18.sp
                    )
                }
            }

            // Show toast message
            toastMessage?.let { message ->
                LaunchedEffect(message) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    toastMessage = null
                }
            }
        }
    }

    // Fetch users from the backend
    private fun fetchUsers(onResult: (List<Triple<String, String, String>>) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        api.getUsernames().enqueue(object : Callback<List<UsernameResponse>> {
            override fun onResponse(call: Call<List<UsernameResponse>>, response: Response<List<UsernameResponse>>) {
                if (response.isSuccessful) {
                    val users = response.body()?.map { Triple(it.username, it.userIcon, it.language) } ?: emptyList()
                    onResult(users)
                } else {
                    onResult(emptyList())
                }
            }

            override fun onFailure(call: Call<List<UsernameResponse>>, t: Throwable) {
                onResult(emptyList())
            }
        })
    }

    // Perform login without PIN
    private fun performPinlessLogin(username: String, language: String, context: Context, onResult: (String) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")  // Ensure this base URL is correct
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val pinlessLoginRequest = PinlessLoginRequest(username)

        api.loginWithoutPin(pinlessLoginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("LoginActivity", "Login response: $loginResponse")

                    if (loginResponse != null) {
                        val userId = loginResponse.userId
                        saveUserSession(context, userId ?: "", language)
                        navigateToHomePage(context)
                        onResult("Login without PIN successful")
                    } else {
                        onResult("Login failed: Empty response")
                    }
                } else {
                    Log.e("LoginActivity", "Login failed with error: ${response.errorBody()?.string()}")
                    onResult("Login failed")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Login API call failed: ${t.message}")
                onResult("An error occurred: ${t.message}")
            }
        })
    }


    // Perform login using PIN
    private fun performPinLogin(username: String, pin: String, language: String, context: Context, onResult: (String) -> Unit) {
        if (username.isEmpty() || pin.isEmpty()) {
            onResult("Username or PIN cannot be empty")
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val pinLoginRequest = PinLoginRequest(username, pin)

        api.loginWithPin(pinLoginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val userId = response.body()?.userId
                    saveUserSession(context, userId ?: "", language)
                    navigateToHomePage(context)
                    onResult("Login with PIN successful")
                } else {
                    onResult("Login with PIN failed")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult("An error occurred: ${t.message}")
            }
        })
    }

    // Save user session
    private fun saveUserSession(context: Context, userId: String, language: String) {
        val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("userId", userId)
            .putString("appLanguage", language)
            .apply()
    }

    // Navigate to the home page
    private fun navigateToHomePage(context: Context) {
        val intent = Intent(context, HomePageActivity::class.java)
        context.startActivity(intent)
    }
}
