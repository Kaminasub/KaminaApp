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


// Retrofit API service for avatars
interface AvatarService {
    @GET("/api/avatars")
    fun getAvatars(): Call<List<String>> // Expect a list of strings (avatar paths)

    @PUT("/api/user_icon/{userId}/icon")
    fun updateUserAvatar(@Path("userId") userId: Int, @Body avatarUpdate: Map<String, String>): Call<Void>
}

class AvatarManager {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(AvatarService::class.java)

    // Function to fetch avatars (list of strings)
    fun fetchAvatars(onSuccess: (List<String>) -> Unit, onFailure: (Throwable) -> Unit) {
        service.getAvatars().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val avatars = response.body() ?: emptyList()
                    Log.d("AvatarManager", "Fetched avatars: $avatars")
                    onSuccess(avatars)
                } else {
                    Log.e("AvatarManager", "Failed to fetch avatars")
                    onFailure(Exception("Failed to fetch avatars"))
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("AvatarManager", "API call failed: ${t.message}")
                onFailure(t)
            }
        })
    }

    // Function to update user avatar using Retrofit
    fun updateUserAvatar(userId: Int, avatarUrl: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val requestBody = mapOf("userIcon" to avatarUrl)

        service.updateUserAvatar(userId, requestBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(Exception("Failed to update avatar"))
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}
