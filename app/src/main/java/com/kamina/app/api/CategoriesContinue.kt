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
import retrofit2.http.Query

// Define the data model for the API response, including the language field
data class ThumbnailData(
    val id: Int,
    val thumbnail: String?,
    val name: String?,
    val language: String?, // Add language field to match API response
    val status: Int
)

// API service interface for fetching continue watching and updating status
interface ContinueApiService {

    @GET("user_entities/{userId}/continue-watching")
    fun getContinueWatching(
        @Path("userId") userId: String,
        @Query("language") language: String
    ): Call<List<ThumbnailData>>

    @GET("entities/{entityId}/status/{userId}")
    fun getUserEntityStatus(
        @Path("userId") userId: Int,
        @Path("entityId") entityId: Int
    ): Call<ThumbnailData>

    @PUT("entities/{entityId}/status/{userId}")
    fun updateUserEntityStatus(
        @Path("userId") userId: Int,
        @Path("entityId") entityId: Int,
        @Body body: Map<String, Int>
    ): Call<Void>

    @GET("user_entities/statuses/{userId}")
    fun getUserEntityStatuses(
        @Path("userId") userId: Int
    ): Call<List<ThumbnailData>>
}

// Function to fetch continue-watching data
fun fetchContinueWatching(userId: String, userLanguage: String, onResult: (List<ThumbnailData>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(ContinueApiService::class.java)

    api.getContinueWatching(userId, userLanguage).enqueue(object : Callback<List<ThumbnailData>> {
        override fun onResponse(
            call: Call<List<ThumbnailData>>,
            response: Response<List<ThumbnailData>>
        ) {
            if (response.isSuccessful) {
                Log.d("CombinedSections", "Successfully fetched continue watching data for user: $userId")
                onResult(response.body())
            } else {
                Log.e("CombinedSections", "Error response code: ${response.code()}, message: ${response.message()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<List<ThumbnailData>>, t: Throwable) {
            Log.e("CombinedSections", "API call failed: ${t.message}")
            onResult(null)
        }
    })
}

// Helper function to convert status code to text
fun getStatusText(status: Int): String {
    return when (status) {
        1 -> "Following"
        2 -> "Completed"
        else -> "Unknown"
    }
}

// Function to call API and update the user status
fun updateUserStatus(userId: Int, entityId: Int, status: Int) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(ContinueApiService::class.java)
    val body = mapOf("status" to status)

    api.updateUserEntityStatus(userId, entityId, body).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("ContinueWatching", "Status updated successfully.")
            } else {
                Log.e("ContinueWatching", "Failed to update status: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("ContinueWatching", "Error: ${t.message}")
        }
    })
}

// Existing function with callback-based approach
fun fetchUserEntityStatus(userId: Int, entityId: Int, onResult: (ThumbnailData?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(ContinueApiService::class.java)

    api.getUserEntityStatus(userId, entityId).enqueue(object : Callback<ThumbnailData> {
        override fun onResponse(
            call: Call<ThumbnailData>,
            response: Response<ThumbnailData>
        ) {
            if (response.isSuccessful) {
                onResult(response.body())
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: Call<ThumbnailData>, t: Throwable) {
            onResult(null)
        }
    })
}

// New suspend function to fetch user entity status directly
suspend fun fetchUserEntityStatusSuspend(userId: Int, entityId: Int): ThumbnailData? {
    return try {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kaminajp.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ContinueApiService::class.java)
        api.getUserEntityStatus(userId, entityId).execute().body()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
