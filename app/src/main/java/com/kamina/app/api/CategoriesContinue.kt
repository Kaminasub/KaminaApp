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

// Define the data model for the API response
data class ThumbnailData(
    val id: Int,
    val thumbnail: String,
    val name: String,
    val status: Int
)

// API service interface for fetching continue watching and updating status
interface CategoriesApiService {

    // Fetches the list of entities the user is currently watching
    @GET("user_entities/{userId}/continue-watching")
    fun getContinueWatching(
        @Path("userId") userId: String
    ): Call<List<ThumbnailData>>

    // Fetches the status of a specific entity (movie/series) for a specific user
    @GET("entities/{entityId}/status/{userId}")
    fun getUserEntityStatus(
        @Path("userId") userId: Int,
        @Path("entityId") entityId: Int
    ): Call<ThumbnailData>  // Returns a ThumbnailData object containing entity status

    // Updates the user's status for a specific entity (e.g., Following, Completed)
    @PUT("entities/{entityId}/status/{userId}")
    fun updateUserEntityStatus(
        @Path("userId") userId: Int,
        @Path("entityId") entityId: Int,
        @Body body: Map<String, Int>  // Body containing the status to update
    ): Call<Void>

    // Fetches the statuses for all entities associated with a user in a batch
    @GET("user_entities/statuses/{userId}")
    fun getUserEntityStatuses(
        @Path("userId") userId: Int
    ): Call<List<ThumbnailData>> // Returns a list of entities with their statuses
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

    val api = retrofit.create(CategoriesApiService::class.java)
    val body = mapOf("status" to status)

    api.updateUserEntityStatus(userId, entityId, body).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("CategoriesContinue", "Status updated successfully.")
            } else {
                Log.e("CategoriesContinue", "Failed to update status: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("CategoriesContinue", "Error: ${t.message}")
        }
    })
}
fun fetchUserEntityStatus(userId: Int, entityId: Int, onResult: (ThumbnailData?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(CategoriesApiService::class.java)

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