package com.kamina.app.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UserApiHelper {
    fun fetchUserIcon(userId: String, onResult: (String?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.getUserIcon(userId).enqueue(object : Callback<UserIconResponse> {
            override fun onResponse(call: Call<UserIconResponse>, response: Response<UserIconResponse>) {
                if (response.isSuccessful) {
                    // Access the userIcon field in the response
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
}
