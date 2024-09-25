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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kamina.app.api.*
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
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // Toast state
        var toastMessage by remember { mutableStateOf<String?>(null) }

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
                modifier = Modifier.size(100.dp)
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
                Button(onClick = { navigateToHomePage(context) }) {
                    Text("Login")
                }
            } else if (selectedUsername != "Select Username") {
                Spacer(modifier = Modifier.height(16.dp))

                if (isPinLogin) {
                    // PIN Input
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) { index ->
                            OutlinedTextField(
                                value = pin.getOrNull(index)?.toString() ?: "",
                                onValueChange = { if (it.length <= 1) pin = pin.padEnd(index, ' ') + it },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(40.dp)
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
