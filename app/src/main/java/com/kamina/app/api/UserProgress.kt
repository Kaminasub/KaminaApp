package com.kamina.app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Data class for user progress
data class UserProgress(
    val id: Int? = null,  // id is nullable to avoid passing it for new entries
    val userId: Int,
    val videoId: Int,
    val currentSeason: Int,
    val currentEpisode: Int,
    val skipIntro: Int,  // Keep as Int in the API response (0 or 1)
    val skipOutro: Int,  // Keep as Int in the API response (0 or 1)
    val watched: Int,    // Keep as Int in the API response (0 or 1)
    val entityId: Int
) {
    // Add custom getters to treat 1 as true and 0 as false
    val isSkipIntro: Boolean
        get() = skipIntro == 1

    val isSkipOutro: Boolean
        get() = skipOutro == 1

    val isWatched: Boolean
        get() = watched == 1
}

// Define the API service interface
interface UserProgressApiService {
    // Endpoint to fetch user progress based on userId and entityId
    @GET("user_progress/{userId}/{entityId}")
    suspend fun getUserProgress(
        @Path("userId") userId: Int,
        @Path("entityId") entityId: Int
    ): UserProgress?

    // POST request to create new user progress
    @POST("user_progress")
    @Headers("Content-Type: application/json")
    suspend fun createProgress(@Body progress: UserProgress): retrofit2.Response<Unit>

    // PUT request to update user progress
    @PUT("user_progress/{id}")
    @Headers("Content-Type: application/json")
    suspend fun updateProgress(@Path("id") id: Int, @Body progress: UserProgress): retrofit2.Response<Unit>
}

// Initialize Retrofit for UserProgress API
private val userProgressRetrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/") // Ensure this base URL is correct
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// API service instance
val userProgressApi = userProgressRetrofit.create(UserProgressApiService::class.java)

// Function to fetch user progress
suspend fun fetchUserProgress(userId: Int, entityId: Int): UserProgress? {
    return try {
        userProgressApi.getUserProgress(userId, entityId)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Function to update user progress
suspend fun updateUserProgress(progress: UserProgress): Boolean {
    return try {
        val response = userProgressApi.updateProgress(progress.id!!, progress)
        response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Function to create new user progress
suspend fun createUserProgress(progress: UserProgress): Boolean {
    return try {
        val response = userProgressApi.createProgress(progress)
        response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
