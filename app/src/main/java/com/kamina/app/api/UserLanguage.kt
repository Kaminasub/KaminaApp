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

// Data model for the language response
data class UserLanguageResponse(
    val language: String
)

// Interface for the User Language API
interface UserLanguageApiService {

    // GET request to fetch the user's language
    @GET("user_language/{userId}")
    fun getUserLanguage(@Path("userId") userId: String): Call<UserLanguageResponse>

    // PUT request to update the user's language
    @PUT("user_language/{userId}")
    fun updateUserLanguage(
        @Path("userId") userId: String,
        @Body languageBody: Map<String, String> // Language sent as a map, e.g. {"language": "en"}
    ): Call<UserLanguageResponse>
}

// Function to fetch the user's language
fun fetchUserLanguage(userId: String, onResult: (String?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(UserLanguageApiService::class.java)

    api.getUserLanguage(userId).enqueue(object : Callback<UserLanguageResponse> {
        override fun onResponse(
            call: Call<UserLanguageResponse>,
            response: Response<UserLanguageResponse>
        ) {
            if (response.isSuccessful) {
                val language = response.body()?.language
                Log.d("UserLanguageAPI", "Language fetched: $language")
                onResult(language)
            } else {
                Log.e("UserLanguageAPI", "Error: ${response.message()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<UserLanguageResponse>, t: Throwable) {
            Log.e("UserLanguageAPI", "Failure: ${t.message}")
            onResult(null)
        }
    })
}

// Function to update the user's language
fun updateUserLanguage(userId: String, newLanguage: String, onResult: (Boolean) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(UserLanguageApiService::class.java)
    val languageBody = mapOf("language" to newLanguage)

    api.updateUserLanguage(userId, languageBody).enqueue(object : Callback<UserLanguageResponse> {
        override fun onResponse(
            call: Call<UserLanguageResponse>,
            response: Response<UserLanguageResponse>
        ) {
            if (response.isSuccessful) {
                Log.d("UserLanguageAPI", "Language updated to: $newLanguage")
                onResult(true)
            } else {
                Log.e("UserLanguageAPI", "Error: ${response.message()}")
                onResult(false)
            }
        }

        override fun onFailure(call: Call<UserLanguageResponse>, t: Throwable) {
            Log.e("UserLanguageAPI", "Failure: ${t.message}")
            onResult(false)
        }
    })
}
