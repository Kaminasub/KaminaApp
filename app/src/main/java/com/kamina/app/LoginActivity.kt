package com.kamina.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kamina.app.api.ApiService
import com.kamina.app.api.LoginRequest
import com.kamina.app.api.LoginResponse
import com.kamina.app.api.PinLoginRequest
import com.kamina.app.api.UsernameResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameDropdown: Spinner
    private lateinit var pinLayout: LinearLayout
    private lateinit var pinEditText1: EditText
    private lateinit var pinEditText2: EditText
    private lateinit var pinEditText3: EditText
    private lateinit var pinEditText4: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var toggleLoginText: TextView

    private var isPinLogin = true // Start with PIN login by default
    private var selectedUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize sharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)


        // Hide status and navigation bars
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        usernameDropdown = findViewById(R.id.usernameDropdown)
        pinLayout = findViewById(R.id.pinLayout)
        pinEditText1 = findViewById(R.id.pinEditText1)
        pinEditText2 = findViewById(R.id.pinEditText2)
        pinEditText3 = findViewById(R.id.pinEditText3)
        pinEditText4 = findViewById(R.id.pinEditText4)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.buttonLogin)
        toggleLoginText = findViewById(R.id.toggleLoginText)

        // Initially hide PIN and password fields
        pinLayout.visibility = View.GONE
        passwordEditText.visibility = View.GONE
        loginButton.visibility = View.GONE
        toggleLoginText.visibility = View.GONE

        // Fetch usernames and populate the dropdown
        fetchUsernames()
        setupPinInputListeners()

        // Toggle between PIN and password login
        toggleLoginText.setOnClickListener {
            isPinLogin = !isPinLogin
            updateLoginMethod()
        }

        loginButton.setOnClickListener {
            if (selectedUsername == "mama") {
                // If mama is selected, login directly without PIN or password
                navigateToHomePage()
            } else if (isPinLogin) {
                val pin = "${pinEditText1.text}${pinEditText2.text}${pinEditText3.text}${pinEditText4.text}"
                performPinLogin(selectedUsername ?: "", pin)
            } else {
                val password = passwordEditText.text.toString()
                performPasswordLogin(selectedUsername ?: "", password)
            }
        }
    }

    private fun fetchUsernames() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")  // Ensure this is the correct base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.getUsernames().enqueue(object : Callback<List<UsernameResponse>> {
            override fun onResponse(call: Call<List<UsernameResponse>>, response: Response<List<UsernameResponse>>) {
                if (response.isSuccessful) {
                    val usernames = response.body()?.map { it.username } ?: emptyList()
                    Log.d("LoginActivity", "Usernames fetched: $usernames")
                    populateUsernameDropdown(usernames)
                } else {
                    Log.e("LoginActivity", "Failed to fetch usernames: ${response.errorBody()?.string()}")
                    showError("Failed to fetch usernames: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<UsernameResponse>>, t: Throwable) {
                Log.e("LoginActivity", "Error fetching usernames: ${t.message}")
                showError("Error fetching usernames: ${t.message}")
            }
        })
    }


    private fun populateUsernameDropdown(usernames: List<String>) {
        // Add "Select Username" as the first item
        val updatedUsernames = listOf("Select Username") + usernames

        // Create an ArrayAdapter with a custom layout for the spinner items
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_item_center, updatedUsernames) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (Select Username)
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.GRAY)  // Set color for "Select Username"
                } else {
                    view.setTextColor(Color.BLACK)  // Set color for other usernames
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.spinner_item_center)
        usernameDropdown.adapter = adapter

        // Set the custom background for the dropdown using reflection
        try {
            val popup = Spinner::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val popupWindow = popup.get(usernameDropdown) as android.widget.ListPopupWindow
            popupWindow.setBackgroundDrawable(getDrawable(R.drawable.spinner_dropdown_background))  // Apply custom dropdown background
        } catch (e: Exception) {
            e.printStackTrace()
        }

        usernameDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUsername = updatedUsernames[position]
                Log.d("LoginActivity", "Selected username: $selectedUsername")

                if (position == 0) {
                    // Hide everything if "Select Username" is selected
                    pinLayout.visibility = View.GONE
                    passwordEditText.visibility = View.GONE
                    loginButton.visibility = View.GONE
                    toggleLoginText.visibility = View.GONE
                } else if (selectedUsername == "mama") {
                    // For "mama", show only the login button
                    pinLayout.visibility = View.GONE
                    passwordEditText.visibility = View.GONE
                    loginButton.visibility = View.VISIBLE
                    toggleLoginText.visibility = View.GONE
                } else {
                    // For other users, show PIN/password options and the login button
                    pinLayout.visibility = View.VISIBLE
                    loginButton.visibility = View.VISIBLE
                    toggleLoginText.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedUsername = null
            }
        }
    }

    private fun updateLoginMethod() {
        if (isPinLogin) {
            // Show PIN fields and hide password field
            pinLayout.visibility = View.VISIBLE
            passwordEditText.visibility = View.GONE
            toggleLoginText.text = getString(R.string.swipe_to_login_password)
        } else {
            // Show password field and hide PIN fields
            pinLayout.visibility = View.GONE
            passwordEditText.visibility = View.VISIBLE
            toggleLoginText.text = getString(R.string.swipe_to_login_pin)
        }
    }

    private fun performPinLogin(username: String, pin: String) {
        Log.d("LoginActivity", "Performing PIN login for user: $username with PIN: $pin")

        if (username.isEmpty() || pin.isEmpty()) {
            showError("Username or PIN cannot be empty")
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
                    // Get the userId from the API response and save it in SharedPreferences
                    val userId = response.body()?.userId
                    saveUserId(userId ?: "")

                    Log.d("LoginActivity", "Login with PIN successful")
                    navigateToHomePage()
                } else {
                    showError("Login with PIN failed. Please check your credentials.")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showError("An error occurred: ${t.message}")
            }
        })
    }

    private fun setupPinInputListeners() {
        pinEditText1.addTextChangedListener(PinTextWatcher(pinEditText1, pinEditText2))
        pinEditText2.addTextChangedListener(PinTextWatcher(pinEditText2, pinEditText3))
        pinEditText3.addTextChangedListener(PinTextWatcher(pinEditText3, pinEditText4))
        pinEditText4.addTextChangedListener(PinTextWatcher(pinEditText4, null))  // Last box, no next box
    }

    // Custom TextWatcher class to handle focus movement
    class PinTextWatcher(private val currentView: EditText, private val nextView: EditText?) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.length == 1) {
                nextView?.requestFocus()  // Move to the next EditText
            } else if (s?.length == 0 && before == 1) {
                currentView.requestFocus()  // Stay in the current EditText
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun performPasswordLogin(username: String, password: String) {
        Log.d("LoginActivity", "Performing password login for user: $username")

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username or password cannot be empty")
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
                    // Get the userId from the API response and save it in SharedPreferences
                    val userId = response.body()?.userId
                    saveUserId(userId ?: "")

                    Log.d("LoginActivity", "Login successful")
                    navigateToHomePage()
                } else {
                    showError("Login failed. Please check your credentials.")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showError("An error occurred: ${t.message}")
            }
        })
    }

    private fun saveUserId(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("userId", userId)
        editor.apply()

        // Log to check if the userId is saved correctly
        val storedUserId = sharedPreferences.getString("userId", null)
        Log.d("LoginActivity", "Stored User ID: $storedUserId")
    }


    private fun navigateToHomePage() {
        val intent = Intent(this, HomePageActivity::class.java)
        intent.putExtra("userId", sharedPreferences.getString("userId", null))
        startActivity(intent)
        finish()
    }


    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}