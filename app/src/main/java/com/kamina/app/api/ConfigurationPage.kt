package com.kamina.app.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

// Data model for avatars
data class UserAvatar(
    val id: Int,
    val avatarPath: String
)

// Data model for updating user details
data class UserProfileUpdate(
    val firstName: String,
    val lastName: String,
    val email: String
)

// Retrofit API service for user profile and avatars
interface UserProfileApiService {
    @PUT("user_profile/{userId}")
    fun updateUserProfile(@Path("userId") userId: String, @Body profileUpdate: UserProfileUpdate): Call<Unit>

    @PUT("user_icon/{userId}/icon")
    fun saveUserIcon(@Path("userId") userId: String, @Body iconPath: Map<String, String>): Call<Unit>

    @PUT("user_icon/{userId}/change-pin") // Correct API endpoint
    fun changeUserPin(@Path("userId") userId: String, @Body pinData: Map<String, String>): Call<Unit>

    // Fetch available avatars
    @GET("avatars")
    fun getAvatars(): Call<List<UserAvatar>>
}

// Initialize Retrofit for API calls
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(UserProfileApiService::class.java)

// Function to fetch avatars
fun fetchAvatars(onSuccess: (List<UserAvatar>) -> Unit, onFailure: (Throwable) -> Unit) {
    api.getAvatars().enqueue(object : Callback<List<UserAvatar>> {
        override fun onResponse(call: Call<List<UserAvatar>>, response: Response<List<UserAvatar>>) {
            if (response.isSuccessful) {
                val avatars = response.body() ?: emptyList()
                onSuccess(avatars)
            } else {
                onFailure(Exception("Failed to fetch avatars"))
            }
        }

        override fun onFailure(call: Call<List<UserAvatar>>, t: Throwable) {
            onFailure(t)
        }
    })
}

// Function to update user profile
fun updateUserProfile(userId: String, firstName: String, lastName: String, email: String) {
    val profileUpdate = UserProfileUpdate(firstName, lastName, email)
    api.updateUserProfile(userId, profileUpdate).enqueue(object : Callback<Unit> {
        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            if (response.isSuccessful) {
                Log.d("UserProfileAPI", "Profile updated successfully")
            }
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            Log.e("UserProfileAPI", "Failed to update profile: ${t.message}")
        }
    })
}

// Function to save user icon
fun saveUserIcon(userId: String, iconPath: String) {
    api.saveUserIcon(userId, mapOf("userIcon" to iconPath)).enqueue(object : Callback<Unit> {
        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            if (response.isSuccessful) {
                Log.d("UserIconAPI", "User icon updated successfully")
            }
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            Log.e("UserIconAPI", "Failed to update user icon: ${t.message}")
        }
    })
}

// Function to change user PIN with a callback for success or failure
fun changeUserPin(userId: String, oldPin: String, newPin: String, onResult: (Boolean) -> Unit) {
    api.changeUserPin(userId, mapOf("oldPin" to oldPin, "newPin" to newPin)).enqueue(object : Callback<Unit> {
        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            if (response.isSuccessful) {
                Log.d("UserPinAPI", "User PIN changed successfully")
                onResult(true) // Call the success callback
            } else {
                Log.e("UserPinAPI", "Failed to change user PIN: ${response.message()}")
                onResult(false) // Call the failure callback
            }
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            Log.e("UserPinAPI", "Failed to change user PIN: ${t.message}")
            onResult(false) // Call the failure callback
        }
    })
}
