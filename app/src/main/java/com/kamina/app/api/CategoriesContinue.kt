package com.kamina.app.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Define the data model for the API response
data class ThumbnailData(
    val id: Int,
    val thumbnail: String,
    val name: String,
    val status: Int
)

// API service interface for fetching continue watching
interface CategoriesApiService {
    @GET("user_entities/{userId}/continue-watching")
    fun getContinueWatching(@Path("userId") userId: String): Call<List<ThumbnailData>>
}

// Function to fetch continue-watching data
fun fetchContinueWatching(userId: String, onResult: (List<ThumbnailData>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(CategoriesApiService::class.java)

    api.getContinueWatching(userId).enqueue(object : Callback<List<ThumbnailData>> {
        override fun onResponse(
            call: Call<List<ThumbnailData>>,
            response: Response<List<ThumbnailData>>
        ) {
            if (response.isSuccessful) {
                onResult(response.body())
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: Call<List<ThumbnailData>>, t: Throwable) {
            onResult(null)
        }
    })
}
