package com.kamina.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.util.Log
import com.kamina.app.api.ApiService
import com.kamina.app.api.LoginRequest
import com.kamina.app.api.LoginResponse
import com.kamina.app.api.PinLoginRequest
import com.kamina.app.api.UsernameResponse
import com.kamina.app.ui.theme.KaminaAppTheme
import kotlinx.coroutines.launch
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen() {
        var selectedUsername by remember { mutableStateOf("Select Username") }
        var pin by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPinLogin by remember { mutableStateOf(true) }
        var isDropdownExpanded by remember { mutableStateOf(false) }
        var usernames by remember { mutableStateOf(listOf("Select Username", "mama")) }
        var isPinVisible by remember { mutableStateOf(false) } // State to manage visibility of the PIN
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // Toast state
        var toastMessage by remember { mutableStateOf<String?>(null) }

        // Focus Requesters
        val focusRequester1 = remember { FocusRequester() }
        val focusRequester2 = remember { FocusRequester() }
        val focusRequester3 = remember { FocusRequester() }
        val focusRequester4 = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        // Show toast if message is available
        toastMessage?.let { message ->
            LaunchedEffect(message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                toastMessage = null // Reset the message after showing
            }
        }

        // Fetch usernames from the API
        LaunchedEffect(Unit) {
            fetchUsernames { fetchedUsernames ->
                usernames = listOf("Select Username") + fetchedUsernames
            }
        }

        // Define the gradient background colors
        val gradientColors = listOf(
            Color(0xFF9B34EF),  // #9b34ef
            Color(0xFF490CB0),  // #490cb0
            Color.Transparent   // Tertiary color
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = gradientColors))  // Apply gradient background
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Skull Icon
            Image(
                painter = painterResource(id = R.drawable.skull),
                contentDescription = "Icon",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Username Dropdown
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedUsername,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Username") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    usernames.forEach { username ->
                        DropdownMenuItem(
                            text = { Text(username) },
                            onClick = {
                                selectedUsername = username
                                isDropdownExpanded = false
                            },
                            enabled = username != "Select Username"
                        )
                    }
                }
            }

            if (selectedUsername == "mama") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    coroutineScope.launch {
                        val mamaUserId = "26"  // Assign mama's userId as 26 from the database
                        saveUserId(context, mamaUserId)  // Save the user ID in SharedPreferences
                        Log.d("LoginActivity", "Logging in as mama with user ID: $mamaUserId")  // Log the login action
                        navigateToHomePage(context)  // Navigate to the home page
                    }
                }) {
                    Text("Login")
                }
            } else if (selectedUsername != "Select Username") {
                Spacer(modifier = Modifier.height(16.dp))

                if (isPinLogin) {
                    // Row to place the PIN input fields and eye icon next to each other
                    Row(
                        horizontalArrangement = Arrangement.Center,  // Center the boxes horizontally
                        verticalAlignment = Alignment.CenterVertically  // Align vertically to the middle
                    ) {
                        // Center the PIN input fields
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),  // Add spacing between boxes
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pin.getOrNull(0)?.toString() ?: "",
                                onValueChange = {
                                    if (it.length == 1) {
                                        pin = it + pin.drop(1)
                                        focusRequester2.requestFocus()
                                    }
                                },
                                visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(40.dp)
                                    .focusRequester(focusRequester1)
                            )

                            OutlinedTextField(
                                value = pin.getOrNull(1)?.toString() ?: "",
                                onValueChange = {
                                    if (it.length == 1) {
                                        pin = pin.take(1) + it + pin.drop(2)
                                        focusRequester3.requestFocus()
                                    }
                                },
                                visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(40.dp)
                                    .focusRequester(focusRequester2)
                            )

                            OutlinedTextField(
                                value = pin.getOrNull(2)?.toString() ?: "",
                                onValueChange = {
                                    if (it.length == 1) {
                                        pin = pin.take(2) + it + pin.drop(3)
                                        focusRequester4.requestFocus()
                                    }
                                },
                                visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(40.dp)
                                    .focusRequester(focusRequester3)
                            )

                            OutlinedTextField(
                                value = pin.getOrNull(3)?.toString() ?: "",
                                onValueChange = {
                                    if (it.length == 1) {
                                        pin = pin.take(3) + it
                                        focusManager.clearFocus()
                                    }
                                },
                                visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(40.dp)
                                    .focusRequester(focusRequester4)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))  // Add spacing between boxes and the eye icon

                        // Eye Icon for PIN visibility toggle
                        IconButton(onClick = { isPinVisible = !isPinVisible }) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN"
                            )
                        }
                    }

                } else {
                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    coroutineScope.launch {
                        if (isPinLogin) {
                            performPinLogin(selectedUsername, pin, context) { message ->
                                toastMessage = message
                            }
                        } else {
                            performPasswordLogin(selectedUsername, password, context) { message ->
                                toastMessage = message
                            }
                        }
                    }
                }) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Login Method
                Text(
                    text = if (isPinLogin) "Swipe to login with password" else "Swipe to login with PIN",
                    color = Color.White,
                    modifier = Modifier.clickable { isPinLogin = !isPinLogin }
                )
            }
        }
    }

    private fun navigateToHomePage(context: Context) {
        val intent = Intent(context, HomePageActivity::class.java)
        context.startActivity(intent)
    }

    private fun performPinLogin(username: String, pin: String, context: Context, onResult: (String) -> Unit) {
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
                    saveUserId(context, userId ?: "")
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

    private fun performPasswordLogin(username: String, password: String, context: Context, onResult: (String) -> Unit) {
        if (username.isEmpty() || password.isEmpty()) {
            onResult("Username or Password cannot be empty")
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val loginRequest = LoginRequest(username, password)

        api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val userId = response.body()?.userId
                    saveUserId(context, userId ?: "")
                    navigateToHomePage(context)
                    onResult("Login successful")
                } else {
                    onResult("Login failed")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult("An error occurred: ${t.message}")
            }
        })
    }

    private fun saveUserId(context: Context, userId: String) {
        val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("userId", userId).apply()

        // Log the userId being saved to check in Logcat
        Log.d("LoginActivity", "User ID saved: $userId")
    }


    private fun fetchUsernames(onSuccess: (List<String>) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.getUsernames().enqueue(object : Callback<List<UsernameResponse>> {
            override fun onResponse(call: Call<List<UsernameResponse>>, response: Response<List<UsernameResponse>>) {
                if (response.isSuccessful) {
                    onSuccess(response.body()?.map { it.username } ?: emptyList())
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<UsernameResponse>>, t: Throwable) {
                onSuccess(emptyList())
            }
        })
    }
}
