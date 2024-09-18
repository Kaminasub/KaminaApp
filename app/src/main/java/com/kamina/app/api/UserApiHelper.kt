package com.kamina.app.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UserApiHelper {
    // Function to fetch the user icon from the backend
    fun fetchUserIcon(userId: String, onResult: (String?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.getUserIcon(userId).enqueue(object : Callback<UserIconResponse> {
            override fun onResponse(
                call: Call<UserIconResponse>,
                response: Response<UserIconResponse>
            ) {
                if (response.isSuccessful) {
                    // Ensure the full URL is constructed
                    val iconUrl = response.body()?.userIcon?.let { userIconPath ->
                        if (!userIconPath.startsWith("http")) {
                            "https://api.kaminajp.com$userIconPath" // Prepend the base URL if needed
                        } else {
                            userIconPath // If it's already a full URL, use it directly
                        }
                    }
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
}
